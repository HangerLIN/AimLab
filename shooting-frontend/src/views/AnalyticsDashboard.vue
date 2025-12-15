<template>
  <div class="dashboard-container">
    <section class="page-header">
      <h1 class="page-title">
        <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 3v18h18"/>
          <rect x="7" y="13" width="3" height="5" rx="1"/>
          <rect x="12" y="9" width="3" height="9" rx="1"/>
          <rect x="17" y="5" width="3" height="13" rx="1"/>
        </svg>
        数据分析概览
      </h1>
      <p class="page-subtitle">快速进入对比与趋势分析，查看训练表现全景</p>
    </section>

    <section class="cards-grid">
      <div class="feature-card">
        <div class="card-icon compare">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 3v18h18"/>
            <rect x="6" y="13" width="4" height="6" rx="1"/>
            <rect x="11" y="9" width="4" height="10" rx="1"/>
            <rect x="16" y="5" width="4" height="14" rx="1"/>
          </svg>
        </div>
        <div class="card-content">
          <h3>运动员对比</h3>
          <p>多维度雷达图与柱状图，快速评估同级别运动员差异。</p>
          <div class="card-actions">
            <el-button type="primary" @click="goCompare">进入对比</el-button>
          </div>
        </div>
      </div>

      <div class="feature-card">
        <div class="card-icon trend">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 17 9 11 13 15 21 7"/>
            <circle cx="9" cy="11" r="1.5"/>
            <circle cx="13" cy="15" r="1.5"/>
            <circle cx="21" cy="7" r="1.5"/>
          </svg>
        </div>
        <div class="card-content">
          <h3>趋势分析</h3>
          <p>周度趋势、移动平均与未来预测，洞察训练提升路径。</p>
          <div class="card-actions">
            <el-button type="success" @click="goTrend">查看趋势</el-button>
          </div>
        </div>
      </div>
    </section>

    <section class="tips-card">
      <h4>使用提示</h4>
      <ul>
        <li>建议先在“运动员对比”中选择级别和时间范围筛选数据。</li>
        <li>趋势分析支持按项目过滤并导出Excel报告。</li>
        <li>确保后端接口 `/api/analytics/*` 已启动并可访问。</li>
      </ul>
    </section>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';

export default {
  name: 'AnalyticsDashboard',
  setup() {
    const router = useRouter();
    const goCompare = () => router.push('/analytics/compare');
    const goTrend = () => router.push('/analytics/trend');

    return { goCompare, goTrend };
  }
};
</script>

<style scoped>
.dashboard-container { max-width: 1200px; margin: 0 auto; padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-title { display: flex; align-items: center; gap: 12px; font-size: 1.75rem; font-weight: 700; color: #1f2937; }
.title-icon { width: 32px; height: 32px; color: #10b981; }
.page-subtitle { color: #6b7280; font-size: 1rem; margin-top: 8px; }

.cards-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 20px; }
.feature-card { display: flex; gap: 16px; padding: 20px; border-radius: 16px; background: white; box-shadow: 0 4px 18px rgba(0,0,0,0.06); align-items: center; }
.card-icon { width: 64px; height: 64px; border-radius: 16px; display: flex; align-items: center; justify-content: center; }
.card-icon.compare { background: #ecfdf3; color: #10b981; }
.card-icon.trend { background: #eef2ff; color: #6366f1; }
.card-icon svg { width: 36px; height: 36px; }
.card-content h3 { margin: 0 0 6px 0; font-size: 1.1rem; color: #111827; }
.card-content p { margin: 0 0 12px 0; color: #6b7280; line-height: 1.5; }
.card-actions { display: flex; gap: 8px; }

.tips-card { background: white; border-radius: 14px; padding: 18px 20px; box-shadow: 0 3px 14px rgba(0,0,0,0.05); }
.tips-card h4 { margin: 0 0 10px 0; color: #111827; font-weight: 600; }
.tips-card ul { margin: 0; padding-left: 18px; color: #4b5563; line-height: 1.6; }

@media (max-width: 640px) {
  .dashboard-container { padding: 16px; }
  .feature-card { flex-direction: column; align-items: flex-start; }
  .card-icon { width: 56px; height: 56px; }
  .card-icon svg { width: 32px; height: 32px; }
}
</style>
