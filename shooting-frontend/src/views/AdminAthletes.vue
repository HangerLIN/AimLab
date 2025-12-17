<template>
  <div class="admin-athletes-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          运动员管理
        </h1>
        <p class="page-subtitle">管理系统中的所有运动员档案</p>
      </div>
      <div class="header-actions">
        <button class="action-btn secondary" @click="exportAthletes('csv')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          导出CSV
        </button>
        <button class="action-btn secondary" @click="exportAthletes('xlsx')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
          导出Excel
        </button>
        <button class="action-btn primary" @click="showImportDialog = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
          导入运动员
        </button>
        <button class="action-btn success" @click="loadAthletes">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :class="{ spinning: loading }">
            <path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/>
            <path d="M21 3v5h-5"/>
          </svg>
          刷新
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon total">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ athletes.length }}</span>
          <span class="stat-label">总运动员数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon approved">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ approvedCount }}</span>
          <span class="stat-label">已审批</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon pending">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ pendingCount }}</span>
          <span class="stat-label">待审批</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon level">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ nationalLevelCount }}</span>
          <span class="stat-label">国家级运动员</span>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-bar">
      <div class="search-box">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/>
          <path d="m21 21-4.35-4.35"/>
        </svg>
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索运动员姓名..."
          @input="filterAthletes"
        />
      </div>
      <div class="filter-group">
        <select v-model="filterStatus" @change="filterAthletes">
          <option value="">全部状态</option>
          <option value="APPROVED">已审批</option>
          <option value="PENDING">待审批</option>
          <option value="REJECTED">已拒绝</option>
        </select>
        <select v-model="filterLevel" @change="filterAthletes">
          <option value="">全部级别</option>
          <option value="国家级">国家级</option>
          <option value="省级">省级</option>
          <option value="市级">市级</option>
          <option value="初级">初级</option>
        </select>
        <select v-model="filterGender" @change="filterAthletes">
          <option value="">全部性别</option>
          <option value="MALE">男</option>
          <option value="FEMALE">女</option>
        </select>
      </div>
    </div>

    <!-- 运动员列表 -->
    <div class="athletes-table-container">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      <template v-else>
        <table v-if="filteredAthletes.length > 0" class="athletes-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>姓名</th>
              <th>性别</th>
              <th>出生日期</th>
              <th>级别</th>
              <th>审批状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="athlete in filteredAthletes" :key="athlete.id">
              <td class="id-cell">{{ athlete.id }}</td>
              <td class="name-cell">
                <div class="athlete-name">
                  <div class="avatar">{{ athlete.name?.charAt(0) || '?' }}</div>
                  <span>{{ athlete.name }}</span>
                </div>
              </td>
              <td>
                <span class="gender-badge" :class="athlete.gender?.toLowerCase()">
                  {{ athlete.gender === 'MALE' ? '男' : athlete.gender === 'FEMALE' ? '女' : '未知' }}
                </span>
              </td>
              <td>{{ formatDate(athlete.birthDate) }}</td>
              <td>
                <span class="level-badge" :class="getLevelClass(athlete.level)">
                  {{ athlete.level || '未设置' }}
                </span>
              </td>
              <td>
                <span class="status-badge" :class="athlete.approvalStatus?.toLowerCase()">
                  {{ getStatusText(athlete.approvalStatus) }}
                </span>
                <span 
                  v-if="athlete.modificationStatus === 'PENDING'" 
                  class="status-badge modification-pending"
                  style="margin-left: 4px;"
                >
                  修改待审
                </span>
              </td>
              <td>{{ formatDateTime(athlete.createdAt) }}</td>
              <td class="actions-cell">
                <button class="icon-btn view" @click="viewAthleteDetail(athlete)" title="查看详情">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                </button>
                <button 
                  v-if="athlete.approvalStatus === 'PENDING'" 
                  class="icon-btn approve" 
                  @click="approveAthlete(athlete)"
                  title="审批通过"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                </button>
                <button 
                  v-if="athlete.approvalStatus === 'PENDING'" 
                  class="icon-btn reject" 
                  @click="rejectAthlete(athlete)"
                  title="拒绝"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"/>
                    <line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                </button>
                <button 
                  v-if="athlete.modificationStatus === 'PENDING'" 
                  class="icon-btn view-modification" 
                  @click="viewModificationDetail(athlete)"
                  title="查看修改内容"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                    <polyline points="10 9 9 9 8 9"/>
                  </svg>
                </button>
                <button class="icon-btn edit" @click="editAthlete(athlete)" title="编辑">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>
                <button class="icon-btn delete" @click="confirmDeleteAthlete(athlete)" title="删除">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <line x1="17" y1="11" x2="23" y2="11"/>
          </svg>
          <h3>暂无运动员数据</h3>
          <p>系统中还没有运动员档案</p>
        </div>
      </template>
    </div>

    <!-- 运动员详情弹窗 -->
    <div v-if="showDetailDialog" class="dialog-overlay" @click.self="showDetailDialog = false">
      <div class="dialog detail-dialog">
        <div class="dialog-header">
          <h2>运动员详情</h2>
          <button class="close-btn" @click="showDetailDialog = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="dialog-body" v-if="selectedAthlete">
          <div class="detail-header">
            <div class="detail-avatar">{{ selectedAthlete.name?.charAt(0) || '?' }}</div>
            <div class="detail-info">
              <h3>{{ selectedAthlete.name }}</h3>
              <span class="status-badge" :class="selectedAthlete.approvalStatus?.toLowerCase()">
                {{ getStatusText(selectedAthlete.approvalStatus) }}
              </span>
            </div>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <label>用户ID</label>
              <span>{{ selectedAthlete.userId }}</span>
            </div>
            <div class="detail-item">
              <label>运动员ID</label>
              <span>{{ selectedAthlete.id }}</span>
            </div>
            <div class="detail-item">
              <label>性别</label>
              <span>{{ selectedAthlete.gender === 'MALE' ? '男' : selectedAthlete.gender === 'FEMALE' ? '女' : '未知' }}</span>
            </div>
            <div class="detail-item">
              <label>出生日期</label>
              <span>{{ formatDate(selectedAthlete.birthDate) }}</span>
            </div>
            <div class="detail-item">
              <label>运动等级</label>
              <span class="level-badge" :class="getLevelClass(selectedAthlete.level)">{{ selectedAthlete.level || '未设置' }}</span>
            </div>
            <div class="detail-item">
              <label>创建时间</label>
              <span>{{ formatDateTime(selectedAthlete.createdAt) }}</span>
            </div>
            <div class="detail-item">
              <label>更新时间</label>
              <span>{{ formatDateTime(selectedAthlete.updatedAt) }}</span>
            </div>
          </div>
          <!-- 生涯统计 -->
          <div v-if="athleteProfile" class="career-stats">
            <h4>生涯统计</h4>
            <div class="stats-row">
              <div class="stat-item">
                <span class="stat-value">{{ athleteProfile.careerTotalShots || 0 }}</span>
                <span class="stat-label">总射击次数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ athleteProfile.careerAverageScore?.toFixed(2) || '0.00' }}</span>
                <span class="stat-label">平均环数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ athleteProfile.careerBestScore?.toFixed(1) || '0.0' }}</span>
                <span class="stat-label">最佳成绩</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ athleteProfile.totalCompetitions || 0 }}</span>
                <span class="stat-label">参赛次数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ athleteProfile.competitionsWon || 0 }}</span>
                <span class="stat-label">冠军次数</span>
              </div>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn secondary" @click="showDetailDialog = false">关闭</button>
          <button class="btn primary" @click="editAthlete(selectedAthlete); showDetailDialog = false">编辑</button>
        </div>
      </div>
    </div>

    <!-- 编辑运动员弹窗 -->
    <div v-if="showEditDialog" class="dialog-overlay" @click.self="showEditDialog = false">
      <div class="dialog edit-dialog">
        <div class="dialog-header">
          <h2>{{ editingAthlete.id ? '编辑运动员' : '新增运动员' }}</h2>
          <button class="close-btn" @click="showEditDialog = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>姓名 <span class="required">*</span></label>
            <input type="text" v-model="editingAthlete.name" placeholder="请输入姓名" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>性别</label>
              <select v-model="editingAthlete.gender">
                <option value="MALE">男</option>
                <option value="FEMALE">女</option>
                <option value="UNKNOWN">未知</option>
              </select>
            </div>
            <div class="form-group">
              <label>出生日期</label>
              <input type="date" v-model="editingAthlete.birthDate" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>运动等级</label>
              <select v-model="editingAthlete.level">
                <option value="初级">初级</option>
                <option value="市级">市级</option>
                <option value="省级">省级</option>
                <option value="国家级">国家级</option>
              </select>
            </div>
            <div class="form-group">
              <label>审批状态</label>
              <select v-model="editingAthlete.approvalStatus">
                <option value="PENDING">待审批</option>
                <option value="APPROVED">已审批</option>
                <option value="REJECTED">已拒绝</option>
              </select>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn secondary" @click="showEditDialog = false">取消</button>
          <button class="btn primary" @click="saveAthlete" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 导入运动员弹窗 -->
    <div v-if="showImportDialog" class="dialog-overlay" @click.self="showImportDialog = false">
      <div class="dialog import-dialog">
        <div class="dialog-header">
          <h2>导入运动员</h2>
          <button class="close-btn" @click="showImportDialog = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="dialog-body">
          <div class="upload-area" 
               @dragover.prevent="dragover = true" 
               @dragleave="dragover = false"
               @drop.prevent="handleFileDrop"
               :class="{ dragover }">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="17 8 12 3 7 8"/>
              <line x1="12" y1="3" x2="12" y2="15"/>
            </svg>
            <p>拖拽文件到此处，或</p>
            <label class="file-select-btn">
              选择文件
              <input type="file" accept=".csv,.xlsx,.xls" @change="handleFileSelect" hidden />
            </label>
            <span class="file-hint">支持 CSV、Excel 格式</span>
          </div>
          <div v-if="importFile" class="selected-file">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            <span>{{ importFile.name }}</span>
            <button @click="importFile = null">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn secondary" @click="showImportDialog = false">取消</button>
          <button class="btn primary" @click="importAthletes" :disabled="!importFile || importing">
            {{ importing ? '导入中...' : '开始导入' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteDialog" class="dialog-overlay" @click.self="showDeleteDialog = false">
      <div class="dialog delete-dialog">
        <div class="dialog-header">
          <h2>确认删除</h2>
        </div>
        <div class="dialog-body">
          <div class="delete-warning">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            <p>确定要删除运动员 <strong>{{ deletingAthlete?.name }}</strong> 吗？</p>
            <span>此操作不可恢复</span>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn secondary" @click="showDeleteDialog = false">取消</button>
          <button class="btn danger" @click="deleteAthlete" :disabled="deleting">
            {{ deleting ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 查看修改内容弹窗 -->
    <div v-if="showModificationDialog" class="dialog-overlay" @click.self="showModificationDialog = false">
      <div class="dialog modification-dialog">
        <div class="dialog-header">
          <h2>档案修改审批</h2>
          <button class="close-btn" @click="showModificationDialog = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="dialog-body" v-if="modificationAthlete">
          <div class="modification-header">
            <div class="detail-avatar">{{ modificationAthlete.name?.charAt(0) || '?' }}</div>
            <div class="modification-info">
              <h3>{{ modificationAthlete.name }}</h3>
              <span class="status-badge modification-pending">待审批修改</span>
            </div>
          </div>
          
          <div class="modification-compare" v-if="pendingModificationData">
            <h4>修改内容对比</h4>
            <table class="compare-table">
              <thead>
                <tr>
                  <th>字段</th>
                  <th>当前值</th>
                  <th>修改为</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="pendingModificationData.name && pendingModificationData.name !== modificationAthlete.name">
                  <td>姓名</td>
                  <td class="old-value">{{ modificationAthlete.name || '-' }}</td>
                  <td class="new-value">{{ pendingModificationData.name }}</td>
                </tr>
                <tr v-if="pendingModificationData.gender && pendingModificationData.gender !== modificationAthlete.gender">
                  <td>性别</td>
                  <td class="old-value">{{ getGenderText(modificationAthlete.gender) }}</td>
                  <td class="new-value">{{ getGenderText(pendingModificationData.gender) }}</td>
                </tr>
                <tr v-if="pendingModificationData.birthDate && pendingModificationData.birthDate !== modificationAthlete.birthDate">
                  <td>出生日期</td>
                  <td class="old-value">{{ formatDate(modificationAthlete.birthDate) }}</td>
                  <td class="new-value">{{ formatDate(pendingModificationData.birthDate) }}</td>
                </tr>
                <tr v-if="pendingModificationData.level && pendingModificationData.level !== modificationAthlete.level">
                  <td>运动等级</td>
                  <td class="old-value">{{ modificationAthlete.level || '-' }}</td>
                  <td class="new-value">{{ pendingModificationData.level }}</td>
                </tr>
                <tr v-if="pendingModificationData.height && pendingModificationData.height !== modificationAthlete.height">
                  <td>身高</td>
                  <td class="old-value">{{ modificationAthlete.height ? modificationAthlete.height + ' cm' : '-' }}</td>
                  <td class="new-value">{{ pendingModificationData.height }} cm</td>
                </tr>
                <tr v-if="pendingModificationData.weight && pendingModificationData.weight !== modificationAthlete.weight">
                  <td>体重</td>
                  <td class="old-value">{{ modificationAthlete.weight ? modificationAthlete.weight + ' kg' : '-' }}</td>
                  <td class="new-value">{{ pendingModificationData.weight }} kg</td>
                </tr>
                <tr v-if="pendingModificationData.dominantHand && pendingModificationData.dominantHand !== modificationAthlete.dominantHand">
                  <td>惯用手</td>
                  <td class="old-value">{{ modificationAthlete.dominantHand || '-' }}</td>
                  <td class="new-value">{{ pendingModificationData.dominantHand }}</td>
                </tr>
                <tr v-if="pendingModificationData.dominantEye && pendingModificationData.dominantEye !== modificationAthlete.dominantEye">
                  <td>主视眼</td>
                  <td class="old-value">{{ modificationAthlete.dominantEye || '-' }}</td>
                  <td class="new-value">{{ pendingModificationData.dominantEye }}</td>
                </tr>
                <tr v-if="pendingModificationData.shootingEvents && pendingModificationData.shootingEvents !== modificationAthlete.shootingEvents">
                  <td>射击项目</td>
                  <td class="old-value">{{ modificationAthlete.shootingEvents || '-' }}</td>
                  <td class="new-value">{{ pendingModificationData.shootingEvents }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="!hasModificationChanges" class="no-changes">
              <p>没有检测到具体的修改内容</p>
              <pre v-if="modificationAthlete.pendingModificationData" class="raw-data">{{ modificationAthlete.pendingModificationData }}</pre>
            </div>
          </div>
          <div v-else class="no-modification-data">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <p>无法解析修改内容数据</p>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn secondary" @click="showModificationDialog = false">取消</button>
          <button class="btn danger" @click="handleRejectModification" :disabled="processingModification">
            {{ processingModification ? '处理中...' : '拒绝修改' }}
          </button>
          <button class="btn primary" @click="handleApproveModification" :disabled="processingModification">
            {{ processingModification ? '处理中...' : '批准修改' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import apiClient from '@/api/index.js';

export default {
  name: 'AdminAthletes',
  
  setup() {
    // 数据状态
    const athletes = ref([]);
    const filteredAthletes = ref([]);
    const loading = ref(false);
    const saving = ref(false);
    const deleting = ref(false);
    const importing = ref(false);
    
    // 搜索和筛选
    const searchQuery = ref('');
    const filterStatus = ref('');
    const filterLevel = ref('');
    const filterGender = ref('');
    
    // 弹窗状态
    const showDetailDialog = ref(false);
    const showEditDialog = ref(false);
    const showImportDialog = ref(false);
    const showDeleteDialog = ref(false);
    const showModificationDialog = ref(false);
    
    // 选中的运动员
    const selectedAthlete = ref(null);
    const athleteProfile = ref(null);
    const editingAthlete = ref({});
    const deletingAthlete = ref(null);
    const modificationAthlete = ref(null);
    const pendingModificationData = ref(null);
    const processingModification = ref(false);
    
    // 导入相关
    const importFile = ref(null);
    const dragover = ref(false);
    
    // 统计数据
    const approvedCount = computed(() => athletes.value.filter(a => a.approvalStatus === 'APPROVED').length);
    const pendingCount = computed(() => {
      // 统计初次档案待审批 + 档案修改待审批
      return athletes.value.filter(a => 
        a.approvalStatus === 'PENDING' || a.modificationStatus === 'PENDING'
      ).length;
    });
    const nationalLevelCount = computed(() => athletes.value.filter(a => a.level === '国家级').length);
    
    // 检查是否有修改变化
    const hasModificationChanges = computed(() => {
      if (!pendingModificationData.value || !modificationAthlete.value) return false;
      const data = pendingModificationData.value;
      const athlete = modificationAthlete.value;
      return (
        (data.name && data.name !== athlete.name) ||
        (data.gender && data.gender !== athlete.gender) ||
        (data.birthDate && data.birthDate !== athlete.birthDate) ||
        (data.level && data.level !== athlete.level) ||
        (data.height && data.height !== athlete.height) ||
        (data.weight && data.weight !== athlete.weight) ||
        (data.dominantHand && data.dominantHand !== athlete.dominantHand) ||
        (data.dominantEye && data.dominantEye !== athlete.dominantEye) ||
        (data.shootingEvents && data.shootingEvents !== athlete.shootingEvents)
      );
    });
    
    // 加载运动员列表
    const loadAthletes = async () => {
      loading.value = true;
      try {
        const res = await apiClient.get('/admin/athletes');
        if (res.success) {
          athletes.value = res.athletes || [];
          filterAthletes();
        } else {
          ElMessage.error(res.message || '加载失败');
        }
      } catch (error) {
        ElMessage.error('加载运动员列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 筛选运动员
    const filterAthletes = () => {
      let result = [...athletes.value];
      
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        result = result.filter(a => a.name?.toLowerCase().includes(query));
      }
      
      if (filterStatus.value) {
        result = result.filter(a => a.approvalStatus === filterStatus.value);
      }
      
      if (filterLevel.value) {
        result = result.filter(a => a.level === filterLevel.value);
      }
      
      if (filterGender.value) {
        result = result.filter(a => a.gender === filterGender.value);
      }
      
      filteredAthletes.value = result;
    };
    
    // 查看运动员详情
    const viewAthleteDetail = async (athlete) => {
      selectedAthlete.value = athlete;
      showDetailDialog.value = true;
      
      // 加载完整档案
      try {
        const res = await apiClient.get(`/athletes/${athlete.id}/profile`);
        if (res.success) {
          athleteProfile.value = res.profile;
        }
      } catch (error) {
        console.error('加载运动员档案失败', error);
      }
    };
    
    // 编辑运动员
    const editAthlete = (athlete) => {
      editingAthlete.value = { ...athlete };
      showEditDialog.value = true;
    };
    
    // 保存运动员
    const saveAthlete = async () => {
      if (!editingAthlete.value.name) {
        ElMessage.warning('请输入姓名');
        return;
      }
      
      saving.value = true;
      try {
        const res = await apiClient.put(`/admin/athletes/${editingAthlete.value.id}`, editingAthlete.value);
        if (res.success) {
          ElMessage.success('保存成功');
          showEditDialog.value = false;
          loadAthletes();
        } else {
          ElMessage.error(res.message || '保存失败');
        }
      } catch (error) {
        ElMessage.error('保存失败');
      } finally {
        saving.value = false;
      }
    };
    
    // 审批运动员
    const approveAthlete = async (athlete) => {
      try {
        const res = await apiClient.put(`/admin/athletes/${athlete.id}/approve`);
        if (res.success) {
          ElMessage.success('审批通过');
          loadAthletes();
        } else {
          ElMessage.error(res.message || '审批失败');
        }
      } catch (error) {
        ElMessage.error('审批失败');
      }
    };
    
    // 拒绝运动员
    const rejectAthlete = async (athlete) => {
      try {
        const res = await apiClient.put(`/admin/athletes/${athlete.id}/reject`);
        if (res.success) {
          ElMessage.success('已拒绝');
          loadAthletes();
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch (error) {
        ElMessage.error('操作失败');
      }
    };

    // 审批档案修改
    const approveModification = async (athlete) => {
      try {
        const res = await apiClient.put(`/admin/athletes/${athlete.id}/approve-modification`, {});
        if (res.success) {
          ElMessage.success('档案修改已审批通过');
          loadAthletes();
        } else {
          ElMessage.error(res.message || '审批失败');
        }
      } catch (error) {
        ElMessage.error('审批失败');
      }
    };

    // 拒绝档案修改
    const rejectModification = async (athlete) => {
      try {
        const res = await apiClient.put(`/admin/athletes/${athlete.id}/reject-modification`);
        if (res.success) {
          ElMessage.success('档案修改已拒绝');
          loadAthletes();
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch (error) {
        ElMessage.error('操作失败');
      }
    };

    // 查看修改详情
    const viewModificationDetail = (athlete) => {
      modificationAthlete.value = athlete;
      // 解析待审批的修改数据
      if (athlete.pendingModificationData) {
        try {
          pendingModificationData.value = JSON.parse(athlete.pendingModificationData);
        } catch (e) {
          console.error('解析修改数据失败', e);
          pendingModificationData.value = null;
        }
      } else {
        pendingModificationData.value = null;
      }
      showModificationDialog.value = true;
    };

    // 处理批准修改（从弹窗）
    const handleApproveModification = async () => {
      if (!modificationAthlete.value) return;
      processingModification.value = true;
      try {
        const res = await apiClient.put(`/admin/athletes/${modificationAthlete.value.id}/approve-modification`, {});
        if (res.success) {
          ElMessage.success('档案修改已审批通过');
          showModificationDialog.value = false;
          loadAthletes();
        } else {
          ElMessage.error(res.message || '审批失败');
        }
      } catch (error) {
        ElMessage.error('审批失败');
      } finally {
        processingModification.value = false;
      }
    };

    // 处理拒绝修改（从弹窗）
    const handleRejectModification = async () => {
      if (!modificationAthlete.value) return;
      processingModification.value = true;
      try {
        const res = await apiClient.put(`/admin/athletes/${modificationAthlete.value.id}/reject-modification`);
        if (res.success) {
          ElMessage.success('档案修改已拒绝');
          showModificationDialog.value = false;
          loadAthletes();
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch (error) {
        ElMessage.error('操作失败');
      } finally {
        processingModification.value = false;
      }
    };

    // 获取性别文本
    const getGenderText = (gender) => {
      const map = {
        'MALE': '男',
        'FEMALE': '女',
        'UNKNOWN': '未知'
      };
      return map[gender] || gender || '-';
    };
    
    // 确认删除
    const confirmDeleteAthlete = (athlete) => {
      deletingAthlete.value = athlete;
      showDeleteDialog.value = true;
    };
    
    // 删除运动员
    const deleteAthlete = async () => {
      deleting.value = true;
      try {
        const res = await apiClient.delete(`/admin/athletes/${deletingAthlete.value.id}`);
        if (res.success) {
          ElMessage.success('删除成功');
          showDeleteDialog.value = false;
          loadAthletes();
        } else {
          ElMessage.error(res.message || '删除失败');
        }
      } catch (error) {
        ElMessage.error('删除失败');
      } finally {
        deleting.value = false;
      }
    };
    
    // 导出运动员
    const exportAthletes = async (format) => {
      try {
        const response = await apiClient.get(`/admin/athletes/export?format=${format}`, {
          responseType: 'blob'
        });
        
        const blob = new Blob([response], { 
          type: format === 'csv' ? 'text/csv' : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `athletes_${new Date().toISOString().split('T')[0]}.${format}`;
        link.click();
        window.URL.revokeObjectURL(url);
        
        ElMessage.success('导出成功');
      } catch (error) {
        ElMessage.error('导出失败');
      }
    };
    
    // 处理文件选择
    const handleFileSelect = (e) => {
      const file = e.target.files[0];
      if (file) {
        importFile.value = file;
      }
    };
    
    // 处理文件拖放
    const handleFileDrop = (e) => {
      dragover.value = false;
      const file = e.dataTransfer.files[0];
      if (file) {
        importFile.value = file;
      }
    };
    
    // 导入运动员
    const importAthletes = async () => {
      if (!importFile.value) return;
      
      importing.value = true;
      try {
        const formData = new FormData();
        formData.append('file', importFile.value);
        
        const res = await apiClient.post('/admin/athletes/import', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        
        if (res.success) {
          ElMessage.success(`导入成功，共导入 ${res.importedCount || 0} 条记录`);
          showImportDialog.value = false;
          importFile.value = null;
          loadAthletes();
        } else {
          ElMessage.error(res.message || '导入失败');
        }
      } catch (error) {
        ElMessage.error('导入失败');
      } finally {
        importing.value = false;
      }
    };
    
    // 格式化日期
    const formatDate = (date) => {
      if (!date) return '-';
      return new Date(date).toLocaleDateString('zh-CN');
    };
    
    const formatDateTime = (date) => {
      if (!date) return '-';
      return new Date(date).toLocaleString('zh-CN');
    };
    
    // 获取状态文本
    const getStatusText = (status) => {
      const map = {
        'APPROVED': '已审批',
        'PENDING': '待审批',
        'REJECTED': '已拒绝'
      };
      return map[status] || status || '未知';
    };
    
    // 获取级别样式类
    const getLevelClass = (level) => {
      const map = {
        '国家级': 'national',
        '省级': 'provincial',
        '市级': 'city',
        '初级': 'beginner'
      };
      return map[level] || 'default';
    };
    
    onMounted(() => {
      loadAthletes();
    });
    
    return {
      athletes,
      filteredAthletes,
      loading,
      saving,
      deleting,
      importing,
      searchQuery,
      filterStatus,
      filterLevel,
      filterGender,
      showDetailDialog,
      showEditDialog,
      showImportDialog,
      showDeleteDialog,
      showModificationDialog,
      selectedAthlete,
      athleteProfile,
      editingAthlete,
      deletingAthlete,
      modificationAthlete,
      pendingModificationData,
      processingModification,
      hasModificationChanges,
      importFile,
      dragover,
      approvedCount,
      pendingCount,
      nationalLevelCount,
      loadAthletes,
      filterAthletes,
      viewAthleteDetail,
      viewModificationDetail,
      editAthlete,
      saveAthlete,
      approveAthlete,
      rejectAthlete,
      approveModification,
      rejectModification,
      handleApproveModification,
      handleRejectModification,
      getGenderText,
      confirmDeleteAthlete,
      deleteAthlete,
      exportAthletes,
      handleFileSelect,
      handleFileDrop,
      importAthletes,
      formatDate,
      formatDateTime,
      getStatusText,
      getLevelClass
    };
  }
};
</script>

<style scoped>
.admin-athletes-container {
  padding: 0;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.page-title svg {
  width: 32px;
  height: 32px;
  color: #10b981;
}

.page-subtitle {
  color: #6b7280;
  margin: 4px 0 0 44px;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 10px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  border: none;
}

.action-btn svg {
  width: 18px;
  height: 18px;
}

.action-btn.primary {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.action-btn.secondary {
  background: #f3f4f6;
  color: #374151;
}

.action-btn.secondary:hover {
  background: #e5e7eb;
}

.action-btn.success {
  background: #ecfdf5;
  color: #059669;
}

.action-btn.success:hover {
  background: #d1fae5;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.stat-icon.total {
  background: #eff6ff;
  color: #3b82f6;
}

.stat-icon.approved {
  background: #ecfdf5;
  color: #10b981;
}

.stat-icon.pending {
  background: #fef3c7;
  color: #f59e0b;
}

.stat-icon.level {
  background: #fef2f2;
  color: #ef4444;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 0.875rem;
  color: #6b7280;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 16px;
  flex: 1;
  max-width: 400px;
}

.search-box svg {
  width: 20px;
  height: 20px;
  color: #9ca3af;
}

.search-box input {
  border: none;
  outline: none;
  flex: 1;
  font-size: 0.95rem;
}

.filter-group {
  display: flex;
  gap: 12px;
}

.filter-group select {
  padding: 10px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: white;
  font-size: 0.95rem;
  cursor: pointer;
}

/* 表格容器 */
.athletes-table-container {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.athletes-table {
  width: 100%;
  border-collapse: collapse;
}

.athletes-table th,
.athletes-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #f3f4f6;
}

.athletes-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.athletes-table tbody tr:hover {
  background: #f9fafb;
}

.id-cell {
  color: #9ca3af;
  font-size: 0.875rem;
}

.athlete-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.gender-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
}

.gender-badge.male {
  background: #eff6ff;
  color: #3b82f6;
}

.gender-badge.female {
  background: #fdf2f8;
  color: #ec4899;
}

.level-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
}

.level-badge.national {
  background: #fef2f2;
  color: #dc2626;
}

.level-badge.provincial {
  background: #fef3c7;
  color: #d97706;
}

.level-badge.city {
  background: #ecfdf5;
  color: #059669;
}

.level-badge.beginner {
  background: #f3f4f6;
  color: #6b7280;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.approved {
  background: #ecfdf5;
  color: #059669;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.modification-pending {
  background: #fef3c7;
  color: #d97706;
  font-size: 11px;
}

.status-badge.rejected {
  background: #fef2f2;
  color: #dc2626;
}

.actions-cell {
  display: flex;
  gap: 8px;
}

.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.icon-btn svg {
  width: 16px;
  height: 16px;
}

.icon-btn.view {
  background: #eff6ff;
  color: #3b82f6;
}

.icon-btn.view:hover {
  background: #dbeafe;
}

.icon-btn.approve {
  background: #ecfdf5;
  color: #10b981;
}

.icon-btn.approve:hover {
  background: #d1fae5;
}

.icon-btn.reject {
  background: #fef3c7;
  color: #f59e0b;
}

.icon-btn.reject:hover {
  background: #fde68a;
}

.icon-btn.edit {
  background: #f3f4f6;
  color: #6b7280;
}

.icon-btn.edit:hover {
  background: #e5e7eb;
}

.icon-btn.delete {
  background: #fef2f2;
  color: #ef4444;
}

.icon-btn.delete:hover {
  background: #fee2e2;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  padding: 60px 20px;
  text-align: center;
  color: #6b7280;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #10b981;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

.empty-state svg {
  width: 64px;
  height: 64px;
  color: #d1d5db;
  margin-bottom: 16px;
}

.empty-state h3 {
  margin: 0 0 8px;
  color: #374151;
}

/* 弹窗 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.dialog {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 560px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.detail-dialog {
  max-width: 640px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
}

.dialog-header h2 {
  margin: 0;
  font-size: 1.25rem;
  color: #1f2937;
}

.close-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: #f3f4f6;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.close-btn:hover {
  background: #e5e7eb;
}

.close-btn svg {
  width: 20px;
  height: 20px;
  color: #6b7280;
}

.dialog-body {
  padding: 24px;
  overflow-y: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f3f4f6;
}

.btn {
  padding: 10px 20px;
  border-radius: 10px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn.primary {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.btn.secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn.secondary:hover {
  background: #e5e7eb;
}

.btn.danger {
  background: #ef4444;
  color: white;
}

.btn.danger:hover {
  background: #dc2626;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 详情弹窗 */
.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.detail-avatar {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 600;
}

.detail-info h3 {
  margin: 0 0 8px;
  font-size: 1.25rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-size: 0.8rem;
  color: #6b7280;
}

.detail-item span {
  font-weight: 500;
  color: #1f2937;
}

.career-stats {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;
}

.career-stats h4 {
  margin: 0 0 16px;
  color: #374151;
}

.stats-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stats-row .stat-item {
  flex: 1;
  min-width: 80px;
  text-align: center;
  padding: 12px;
  background: #f9fafb;
  border-radius: 10px;
}

.stats-row .stat-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #10b981;
  display: block;
}

.stats-row .stat-label {
  font-size: 0.75rem;
  color: #6b7280;
}

/* 表单 */
.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #374151;
}

.required {
  color: #ef4444;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 0.95rem;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* 导入弹窗 */
.upload-area {
  border: 2px dashed #e5e7eb;
  border-radius: 16px;
  padding: 40px 20px;
  text-align: center;
  transition: all 0.25s ease;
}

.upload-area.dragover {
  border-color: #10b981;
  background: #ecfdf5;
}

.upload-area svg {
  width: 48px;
  height: 48px;
  color: #9ca3af;
  margin-bottom: 16px;
}

.upload-area p {
  color: #6b7280;
  margin: 0 0 12px;
}

.file-select-btn {
  display: inline-block;
  padding: 10px 20px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 500;
  transition: transform 0.2s;
}

.file-select-btn:hover {
  transform: translateY(-2px);
}

.file-hint {
  display: block;
  margin-top: 12px;
  font-size: 0.8rem;
  color: #9ca3af;
}

.selected-file {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 10px;
}

.selected-file svg {
  width: 24px;
  height: 24px;
  color: #10b981;
}

.selected-file span {
  flex: 1;
  color: #374151;
}

.selected-file button {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: none;
  background: #fee2e2;
  color: #ef4444;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.selected-file button svg {
  width: 16px;
  height: 16px;
  color: #ef4444;
}

/* 删除确认 */
.delete-warning {
  text-align: center;
}

.delete-warning svg {
  width: 48px;
  height: 48px;
  color: #f59e0b;
  margin-bottom: 16px;
}

.delete-warning p {
  margin: 0 0 8px;
  font-size: 1.1rem;
  color: #374151;
}

.delete-warning span {
  color: #9ca3af;
  font-size: 0.9rem;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .filter-bar {
    flex-direction: column;
  }
  
  .search-box {
    max-width: 100%;
  }
  
  .filter-group {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .filter-group select {
    flex: 1;
    min-width: 120px;
  }
  
  .athletes-table {
    font-size: 0.875rem;
  }
  
  .athletes-table th,
  .athletes-table td {
    padding: 10px 12px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

/* 修改审批弹窗 */
.modification-dialog {
  max-width: 700px;
}

.modification-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.modification-info h3 {
  margin: 0 0 8px;
  font-size: 1.25rem;
}

.modification-compare h4 {
  margin: 0 0 16px;
  color: #374151;
  font-size: 1rem;
}

.compare-table {
  width: 100%;
  border-collapse: collapse;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.compare-table th,
.compare-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f3f4f6;
}

.compare-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.compare-table th:first-child {
  width: 100px;
}

.compare-table td:first-child {
  font-weight: 500;
  color: #6b7280;
}

.old-value {
  color: #9ca3af;
  text-decoration: line-through;
}

.new-value {
  color: #10b981;
  font-weight: 500;
}

.no-changes {
  padding: 20px;
  text-align: center;
  color: #6b7280;
  background: #f9fafb;
  border-radius: 10px;
}

.no-changes p {
  margin: 0 0 12px;
}

.raw-data {
  text-align: left;
  background: #1f2937;
  color: #10b981;
  padding: 12px;
  border-radius: 8px;
  font-size: 0.8rem;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.no-modification-data {
  padding: 40px 20px;
  text-align: center;
  color: #6b7280;
}

.no-modification-data svg {
  width: 48px;
  height: 48px;
  color: #f59e0b;
  margin-bottom: 16px;
}

.no-modification-data p {
  margin: 0;
}

.icon-btn.view-modification {
  background: #fef3c7;
  color: #d97706;
}

.icon-btn.view-modification:hover {
  background: #fde68a;
}
</style>
