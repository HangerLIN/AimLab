const { chromium } = require('playwright');

async function testSimpleProfile() {
    console.log('🚀 简化测试：登录 + 查询档案\n');
    
    const browser = await chromium.launch({ headless: false });
    const context = await browser.newContext();
    const page = await context.newPage();
    
    // 监听网络请求
    page.on('request', request => {
        if (request.url().includes('/api/')) {
            console.log(`📤 ${request.method()} ${request.url()}`);
            const headers = request.headers();
            if (headers['aimlab-token']) {
                console.log(`   Token: ${headers['aimlab-token'].substring(0, 20)}...`);
            }
        }
    });
    
    page.on('response', async response => {
        if (response.url().includes('/api/')) {
            console.log(`📥 ${response.status()} ${response.url()}`);
            if (response.url().includes('login') || response.url().includes('my-profile')) {
                try {
                    const body = await response.text();
                    const json = JSON.parse(body);
                    if (json.tokenInfo) {
                        console.log(`   返回 token: ${json.tokenInfo.tokenValue.substring(0, 20)}...`);
                        console.log(`   loginId: ${json.tokenInfo.loginId}`);
                    }
                    if (json.profile) {
                        console.log(`   profile userId: ${json.profile.userId}`);
                        console.log(`   profile name: ${json.profile.name}`);
                    }
                } catch (e) {}
            }
        }
    });
    
    try {
        // 1. 访问登录页并清除缓存
        console.log('步骤 1: 访问登录页并清除缓存');
        await page.goto('http://localhost:5173/login');
        await page.waitForLoadState('networkidle');
        
        await page.evaluate(() => {
            localStorage.clear();
            sessionStorage.clear();
        });
        console.log('✅ 已清除缓存\n');
        
        // 2. 填写登录信息
        console.log('步骤 2: 填写登录信息');
        await page.fill('input[type="text"]', 'AAA');
        await page.fill('input[type="password"]', 'AAAAAA');
        console.log('✅ 已填写用户名和密码\n');
        
        // 3. 点击登录
        console.log('步骤 3: 点击登录');
        await page.click('button:has-text("登录")');
        await page.waitForTimeout(3000);
        
        const afterLoginUrl = page.url();
        console.log(`✅ 登录后 URL: ${afterLoginUrl}\n`);
        
        // 4. 访问档案页面
        console.log('步骤 4: 访问档案页面');
        await page.goto('http://localhost:5173/profile');
        await page.waitForTimeout(3000);
        
        console.log('\n✅ 测试完成！浏览器将保持打开30秒以便查看...');
        await page.waitForTimeout(30000);
        
    } catch (error) {
        console.error('\n❌ 测试出错:', error.message);
    }
    
    await browser.close();
}

testSimpleProfile().catch(console.error);







