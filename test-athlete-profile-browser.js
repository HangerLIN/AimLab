const { chromium } = require('playwright');

async function testAthleteProfile() {
    console.log('🚀 启动 Playwright 测试...\n');
    
    const browser = await chromium.launch({ 
        headless: false,
        slowMo: 500 // 减慢操作速度以便观察
    });
    const context = await browser.newContext();
    const page = await context.newPage();
    
    // 存储控制台日志
    const consoleLogs = [];
    page.on('console', msg => {
        const logEntry = `[${msg.type().toUpperCase()}] ${msg.text()}`;
        consoleLogs.push(logEntry);
        console.log('📝 Console:', logEntry);
    });
    
    // 存储网络请求
    const networkRequests = [];
    page.on('request', request => {
        const requestInfo = {
            url: request.url(),
            method: request.method(),
            headers: request.headers(),
            postData: request.postData()
        };
        networkRequests.push(requestInfo);
        console.log(`📤 Request: ${request.method()} ${request.url()}`);
    });
    
    // 存储网络响应
    const networkResponses = [];
    page.on('response', async response => {
        const responseInfo = {
            url: response.url(),
            status: response.status(),
            statusText: response.statusText(),
            headers: response.headers()
        };
        
        // 只获取 API 响应的 body
        if (response.url().includes('/api/')) {
            try {
                const body = await response.text();
                responseInfo.body = body;
            } catch (e) {
                responseInfo.body = '[无法读取响应体]';
            }
        }
        
        networkResponses.push(responseInfo);
        console.log(`📥 Response: ${response.status()} ${response.url()}`);
    });
    
    try {
        // 步骤 0: 清除缓存并登录
        console.log('\n📍 步骤 0: 清除浏览器缓存...');
        await page.goto('http://localhost:5173/login');
        await page.waitForLoadState('networkidle');
        
        // 清除 localStorage 中的所有登录信息
        await page.evaluate(() => {
            localStorage.clear();
            console.log('✅ localStorage 已清除');
        });
        console.log('✅ 浏览器缓存已清除');
        
        await page.waitForTimeout(1000);
        
        console.log('\n📍 步骤 1: 使用真实账户登录...');
        console.log('📝 填写登录信息...');
        console.log('   用户名: AAA');
        console.log('   密码: AAAAAA');
        
        // 填写用户名
        const usernameInput = await page.locator('input[placeholder*="用户名"], input[type="text"]').first();
        if (await usernameInput.count() > 0) {
            await usernameInput.fill('AAA');
            console.log('✅ 已填写用户名: AAA');
        } else {
            console.log('❌ 未找到用户名输入框');
        }
        
        // 填写密码
        const passwordInput = await page.locator('input[placeholder*="密码"], input[type="password"]').first();
        if (await passwordInput.count() > 0) {
            await passwordInput.fill('AAAAAA');
            console.log('✅ 已填写密码');
        } else {
            console.log('❌ 未找到密码输入框');
        }
        
        // 点击登录按钮
        const loginButton = await page.locator('button:has-text("登录")');
        if (await loginButton.count() > 0) {
            console.log('🔘 点击登录按钮...');
            await loginButton.click();
            await page.waitForTimeout(3000); // 等待登录完成
            
            const afterLoginUrl = page.url();
            console.log('登录后 URL:', afterLoginUrl);
            
            if (afterLoginUrl.includes('/login')) {
                console.log('❌ 登录失败，仍在登录页');
                console.log('⚠️ 请检查账户是否存在或密码是否正确');
            } else {
                console.log('✅ 登录成功！');
            }
        } else {
            console.log('❌ 未找到登录按钮');
        }
        
        // 步骤 2: 访问运动员档案页面
        console.log('\n📍 步骤 2: 访问运动员档案页面...');
        await page.goto('http://localhost:5173/profile');
        await page.waitForLoadState('networkidle');
        await page.waitForTimeout(2000);
        
        // 检查当前 URL
        const currentUrl = page.url();
        console.log('当前页面 URL:', currentUrl);
        
        // 检查页面标题
        const title = await page.title();
        console.log('页面标题:', title);
        
        // 截图
        await page.screenshot({ path: 'test-screenshot-1.png', fullPage: true });
        console.log('✅ 已保存截图: test-screenshot-1.png');
        
        // 步骤 3: 检查是否已有档案
        console.log('\n📍 步骤 3: 检查现有档案...');
        await page.waitForTimeout(2000);
        
        // 获取页面的所有按钮文本用于调试
        const buttons = await page.locator('button').allTextContents();
        console.log('页面上的所有按钮:', buttons);
        
        // 查找"创建档案"按钮
        const createButton = await page.locator('button:has-text("创建档案")');
        if (await createButton.count() > 0) {
            console.log('🔘 找到"创建档案"按钮，点击打开对话框...');
            await createButton.click();
            await page.waitForTimeout(1000);
            await page.screenshot({ path: 'test-screenshot-2-dialog.png', fullPage: true });
            console.log('✅ 创建档案对话框已打开，截图已保存');
        } else {
            console.log('⚠️ 未找到"创建档案"按钮，可能已有档案');
            // 尝试点击编辑按钮
            const editButton = await page.locator('button:has-text("编辑档案")');
            if (await editButton.count() > 0) {
                console.log('📝 已有档案，点击"编辑档案"按钮...');
                await editButton.click();
                await page.waitForTimeout(1000);
                await page.screenshot({ path: 'test-screenshot-2-edit.png', fullPage: true });
                console.log('✅ 编辑对话框已打开，截图已保存');
            }
        }
        
        // 步骤 4: 在对话框中填写运动员信息
        console.log('\n📍 步骤 4: 填写运动员信息...');
        await page.waitForTimeout(500);
        
        // 填写姓名 - 在对话框中
        const nameInput = await page.locator('.el-dialog input[placeholder="请输入姓名"]');
        if (await nameInput.count() > 0) {
            const testName = '测试运动员_' + Date.now();
            await nameInput.fill(testName);
            console.log('✅ 填写姓名:', testName);
        } else {
            console.log('❌ 未找到姓名输入框');
        }
        
        // 选择性别 - 在对话框中
        try {
            const genderSelect = await page.locator('.el-dialog .el-select');
            if (await genderSelect.count() > 0) {
                await genderSelect.click();
                await page.waitForTimeout(500);
                // 等待下拉选项出现
                await page.locator('.el-option:has-text("男")').first().click();
                console.log('✅ 选择性别: 男');
            }
        } catch (e) {
            console.log('⚠️ 性别选择失败:', e.message);
        }
        
        // 填写等级（可选）
        const levelInput = await page.locator('.el-dialog input[placeholder="请输入等级"]');
        if (await levelInput.count() > 0) {
            await levelInput.fill('初级');
            console.log('✅ 填写等级: 初级');
        }
        
        // 步骤 5: 提交表单
        console.log('\n📍 步骤 5: 提交表单...');
        await page.waitForTimeout(1000);
        
        // 在对话框中查找"创建"按钮
        const submitButton = await page.locator('.el-dialog button:has-text("创建"), .el-dialog button:has-text("保存")');
        if (await submitButton.count() > 0) {
            console.log('🔘 找到提交按钮，点击...');
            await submitButton.first().click();
            await page.waitForTimeout(3000); // 等待响应
            console.log('✅ 已点击提交按钮');
        } else {
            console.log('❌ 未找到提交按钮');
        }
        
        // 步骤 6: 检查结果
        console.log('\n📍 步骤 6: 检查结果...');
        await page.waitForTimeout(2000);
        
        // 截图最终状态
        await page.screenshot({ path: 'test-screenshot-3-final.png', fullPage: true });
        console.log('✅ 已保存最终截图: test-screenshot-3-final.png');
        
        // 查找成功提示
        const successMessage = await page.locator('text=/保存成功|创建成功/');
        if (await successMessage.count() > 0) {
            console.log('✅✅✅ 运动员档案创建成功！');
        } else {
            console.log('⚠️ 未检测到成功提示');
        }
        
    } catch (error) {
        console.error('\n❌ 测试过程中发生错误:', error.message);
    }
    
    // 输出汇总报告
    console.log('\n' + '='.repeat(70));
    console.log('📊 测试报告');
    console.log('='.repeat(70));
    
    console.log('\n📝 控制台日志汇总 (' + consoleLogs.length + ' 条):');
    console.log('-'.repeat(70));
    consoleLogs.slice(-20).forEach(log => console.log(log)); // 只显示最后 20 条
    
    console.log('\n📤 API 请求汇总:');
    console.log('-'.repeat(70));
    const apiRequests = networkRequests.filter(req => req.url.includes('/api/'));
    apiRequests.forEach(req => {
        console.log(`${req.method} ${req.url}`);
        if (req.postData) {
            console.log(`  Body: ${req.postData.substring(0, 200)}...`);
        }
    });
    
    console.log('\n📥 API 响应汇总:');
    console.log('-'.repeat(70));
    const apiResponses = networkResponses.filter(res => res.url.includes('/api/'));
    apiResponses.forEach(res => {
        console.log(`${res.status} ${res.url}`);
        if (res.body) {
            try {
                const parsed = JSON.parse(res.body);
                console.log(`  Response: ${JSON.stringify(parsed, null, 2)}`);
            } catch {
                console.log(`  Response: ${res.body.substring(0, 200)}...`);
            }
        }
    });
    
    console.log('\n' + '='.repeat(70));
    console.log('测试完成！浏览器将在 10 秒后关闭...');
    console.log('='.repeat(70) + '\n');
    
    // 保持浏览器打开 10 秒以便查看结果
    await page.waitForTimeout(10000);
    
    await browser.close();
}

// 运行测试
testAthleteProfile().catch(console.error);

