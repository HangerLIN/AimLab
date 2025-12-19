<template>
  <div class="shooting-target-container">
    <svg 
      ref="targetSvg"
      class="shooting-target" 
      :class="{ readonly: readonly }"
      :width="size" 
      :height="size" 
      viewBox="0 0 200 200"
      @click="handleTargetClick"
    >
      <!-- 靶环 -->
      <circle cx="100" cy="100" r="100" fill="#f0f0f0" stroke="#000" />
      <circle cx="100" cy="100" r="90" fill="#f0f0f0" stroke="#000" />
      <circle cx="100" cy="100" r="80" fill="#f0f0f0" stroke="#000" />
      <circle cx="100" cy="100" r="70" fill="#f0f0f0" stroke="#000" />
      <circle cx="100" cy="100" r="60" fill="#f0f0f0" stroke="#000" />
      <circle cx="100" cy="100" r="50" fill="#f0f0f0" stroke="#000" />
      <circle cx="100" cy="100" r="40" fill="#000" stroke="#000" />
      <circle cx="100" cy="100" r="30" fill="#000" stroke="#fff" />
      <circle cx="100" cy="100" r="20" fill="#000" stroke="#fff" />
      <circle cx="100" cy="100" r="10" fill="#000" stroke="#fff" />
      
      <!-- 中心点 -->
      <circle cx="100" cy="100" r="1" fill="#fff" />
      
      <!-- 环数文本 -->
      <text x="190" y="100" text-anchor="middle" fill="#000" font-size="10">1</text>
      <text x="170" y="100" text-anchor="middle" fill="#000" font-size="10">2</text>
      <text x="150" y="100" text-anchor="middle" fill="#000" font-size="10">3</text>
      <text x="130" y="100" text-anchor="middle" fill="#000" font-size="10">4</text>
      <text x="110" y="100" text-anchor="middle" fill="#000" font-size="10">5</text>
      <text x="90" y="100" text-anchor="middle" fill="#fff" font-size="10">6</text>
      <text x="70" y="100" text-anchor="middle" fill="#fff" font-size="10">7</text>
      <text x="50" y="100" text-anchor="middle" fill="#fff" font-size="10">8</text>
      <text x="30" y="100" text-anchor="middle" fill="#fff" font-size="10">9</text>
      <text x="15" y="100" text-anchor="middle" fill="#fff" font-size="10">10</text>
      
      <!-- 弹孔 -->
      <g v-for="(record, index) in records" :key="index">
        <circle 
          :cx="record.x" 
          :cy="record.y" 
          r="3" 
          fill="red" 
          stroke="#fff" 
          stroke-width="0.5"
        />
      </g>
    </svg>
    
    <div v-if="displayScore !== null" class="last-score" :class="{ 'limit-reached': reachedLimit }">
      最后得分: {{ displayScore }}
      <span v-if="reachedLimit" class="limit-badge">（已达上限）</span>
    </div>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus';

export default {
  name: 'ShootingTarget',
  
  props: {
    records: {
      type: Array,
      default: () => []
    },
    size: {
      type: Number,
      default: 400
    },
    interactive: {
      type: Boolean,
      default: true
    },
    readonly: {
      type: Boolean,
      default: false
    },
    // 是否已达到射击上限
    reachedLimit: {
      type: Boolean,
      default: false
    },
    // 最大射击次数（用于提示信息）
    maxShots: {
      type: Number,
      default: 10
    }
  },
  
  emits: ['shot'],
  
  data() {
    return {
      lastScore: null,
      // 记录达到上限前的最后得分（上限后不再更新）
      frozenLastScore: null
    };
  },
  
  watch: {
    records: {
      handler(newRecords) {
        if (newRecords.length > 0 && !this.reachedLimit) {
          this.lastScore = newRecords[newRecords.length - 1].score;
        }
      },
      immediate: true,
      deep: true
    },
    // 当达到上限时，冻结最后得分
    reachedLimit(newVal) {
      if (newVal && this.lastScore !== null) {
        this.frozenLastScore = this.lastScore;
      }
    }
  },
  
  computed: {
    displayScore() {
      // 如果达到上限，显示冻结的得分
      if (this.reachedLimit && this.frozenLastScore !== null) {
        return this.frozenLastScore;
      }
      return this.lastScore;
    }
  },
  
  methods: {
    handleTargetClick(event) {
      console.log('🎯 靶子被点击！interactive =', this.interactive, 'readonly =', this.readonly, 'reachedLimit =', this.reachedLimit);
      
      // 如果已达到射击上限，弹窗提示
      if (this.reachedLimit) {
        console.warn('⚠️ 已达到射击次数上限');
        ElMessage.warning(`您已达到本场比赛的射击次数上限（${this.maxShots}次），无法继续射击`);
        return;
      }
      
      // 如果是只读模式或不可交互，阻止射击
      if (!this.interactive || this.readonly) {
        console.warn('⚠️ 靶子不可交互，射击被阻止');
        return;
      }
      
      // 获取SVG元素的位置和尺寸
      const svgRect = this.$refs.targetSvg.getBoundingClientRect();
      
      // 计算点击位置相对于SVG的坐标
      const svgX = event.clientX - svgRect.left;
      const svgY = event.clientY - svgRect.top;
      
      // 将坐标转换为SVG内部坐标系统 (0-200)
      const x = (svgX / svgRect.width) * 200;
      const y = (svgY / svgRect.height) * 200;
      
      // 计算点击位置与中心点的距离
      const centerX = 100;
      const centerY = 100;
      const distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
      
      // 计算得分 (距离越近，分数越高)
      let score = 0;
      if (distance <= 10) score = 10;
      else if (distance <= 20) score = 9;
      else if (distance <= 30) score = 8;
      else if (distance <= 40) score = 7;
      else if (distance <= 50) score = 6;
      else if (distance <= 60) score = 5;
      else if (distance <= 70) score = 4;
      else if (distance <= 80) score = 3;
      else if (distance <= 90) score = 2;
      else if (distance <= 100) score = 1;
      
      // 创建新记录对象
      const newRecord = {
        x,
        y,
        score,
        timestamp: new Date().toISOString()
      };
      
      // 发送事件
      this.$emit('shot', newRecord);
      
      // 更新最后得分
      this.lastScore = score;
    }
  }
};
</script>

<style scoped>
.shooting-target-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 20px 0;
}

.shooting-target {
  cursor: crosshair;
  border: 1px solid #ccc;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.shooting-target.readonly {
  cursor: default;
}

.last-score {
  margin-top: 10px;
  font-size: 18px;
  font-weight: bold;
}

.last-score.limit-reached {
  color: #f44336;
}

.limit-badge {
  font-size: 14px;
  color: #ff9800;
  margin-left: 5px;
}
</style> 