const { Client } = require('./shooting-frontend/node_modules/@stomp/stompjs');
const SockJS = require('./shooting-frontend/node_modules/sockjs-client');
const { exec } = require('child_process');

const token = process.argv[2];
const competitionId = process.argv[3];
const athleteToken = process.argv[4] || token;

if (!token || !competitionId) {
  console.error('Usage: node websocket_status_test.js <token> <competitionId> [athleteToken]');
  process.exit(1);
}

function runCommand(cmd) {
  return new Promise((resolve, reject) => {
    exec(cmd, { maxBuffer: 1024 * 1024 }, (error, stdout, stderr) => {
      if (error) {
        console.error('CMD_ERROR', cmd, stderr.trim());
        return reject(error);
      }
      console.log('CMD_OUTPUT', cmd, stdout.trim());
      resolve(stdout.trim());
    });
  });
}

async function triggerActions() {
  try {
    await runCommand(`curl -s -X POST http://localhost:8083/api/competitions/${competitionId}/start`);
    await new Promise(r => setTimeout(r, 1500));
    await runCommand(`curl -s -X POST http://localhost:8083/api/competitions/${competitionId}/pause`);
    await new Promise(r => setTimeout(r, 1500));
    await runCommand(`curl -s -X POST http://localhost:8083/api/competitions/${competitionId}/resume`);
    await new Promise(r => setTimeout(r, 1500));
    await runCommand(`curl -s -X POST http://localhost:8083/api/records/competition -H 'Content-Type: application/json' -H 'aimlab-token: ${athleteToken}' -d '{"competitionId":${competitionId},"score":9.8}'`);
    await new Promise(r => setTimeout(r, 1500));
    await runCommand(`curl -s -X POST http://localhost:8083/api/competitions/${competitionId}/complete`);
  } catch (err) {
    console.error('ACTIONS_FAILED', err.message);
  }
}

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8083/ws?token=' + encodeURIComponent(token)),
  connectHeaders: {
    'aimlab-token': token
  },
  debug: () => {},
  reconnectDelay: 0,
  onConnect: () => {
    console.log('CONNECTED');
    client.subscribe(`/topic/competition/${competitionId}/status`, (frame) => {
      try {
        const body = JSON.parse(frame.body);
        console.log('STATUS_MESSAGE', JSON.stringify(body));
      } catch (err) {
        console.log('STATUS_MESSAGE_RAW', frame.body);
      }
    });
    client.subscribe(`/topic/competition/${competitionId}`, (frame) => {
      try {
        const body = JSON.parse(frame.body);
        console.log('GENERIC_MESSAGE', JSON.stringify(body));
      } catch (err) {
        console.log('GENERIC_MESSAGE_RAW', frame.body);
      }
    });
    triggerActions().then(() => {
      setTimeout(() => {
        client.deactivate();
        setTimeout(() => process.exit(0), 500);
      }, 5000);
    });
  },
  onStompError: (frame) => {
    console.error('STOMP_ERROR', frame.headers['message']);
  }
});

client.activate();
