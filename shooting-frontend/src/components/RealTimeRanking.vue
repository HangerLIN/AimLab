<template>
  <div class="ranking-container">
    <h3>实时排名</h3>
    
    <div v-if="!rankingData || rankingData.length === 0" class="no-data">
      暂无排名数据
    </div>
    
    <table v-else class="ranking-table">
      <thead>
        <tr>
          <th>排名</th>
          <th>选手</th>
          <th>总环数</th>
          <th>平均分</th>
          <th>射击数</th>
        </tr>
      </thead>
      <tbody>
        <tr 
          v-for="(athlete, index) in rankingData" 
          :key="athlete.athleteId"
          :class="{ 'current-user': athlete.athleteId === currentUserId }"
        >
          <td class="rank">{{ index + 1 }}</td>
          <td class="name">{{ athlete.name }}</td>
          <td class="score">{{ athlete.totalScore }}</td>
          <td class="average">{{ athlete.averageScore ? athlete.averageScore.toFixed(2) : '0.00' }}</td>
          <td class="shots">{{ athlete.shotCount || 0 }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'RealTimeRanking',
  
  props: {
    rankingData: {
      type: Array,
      default: () => []
    },
    currentUserId: {
      type: [Number, String],
      default: 1 // 默认值，实际应用中应该从用户store获取
    }
  }
};
</script>

<style scoped>
.ranking-container {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h3 {
  text-align: center;
  margin-bottom: 15px;
  color: #333;
}

.no-data {
  text-align: center;
  padding: 20px;
  color: #666;
  font-style: italic;
}

.ranking-table {
  width: 100%;
  border-collapse: collapse;
}

.ranking-table th,
.ranking-table td {
  padding: 10px;
  text-align: center;
  border-bottom: 1px solid #ddd;
}

.ranking-table th {
  background-color: #4CAF50;
  color: white;
  font-weight: bold;
}

.ranking-table tr:nth-child(even) {
  background-color: #f2f2f2;
}

.ranking-table tr:hover {
  background-color: #e9e9e9;
}

.current-user {
  background-color: #e8f5e9 !important;
  font-weight: bold;
}

.rank {
  font-weight: bold;
  width: 60px;
}

.name {
  text-align: left;
}

.score {
  font-weight: bold;
  color: #4CAF50;
}

.average {
  color: #2196F3;
}

.shots {
  color: #FF9800;
}
</style> 