import axios from 'axios'

const API_BASE = '/api'

export const rankingAPI = {
  // 获取全站排行榜
  getOverallRanking(limit = 100) {
    return axios.get(`${API_BASE}/rankings/overall`, {
      params: { limit }
    })
  },

  // 按等级获取排行榜
  getRankingByLevel(level, limit = 50) {
    return axios.get(`${API_BASE}/rankings/by-level`, {
      params: { level, limit }
    })
  },

  // 获取月度排行
  getMonthlyRanking(year = null, month = null, limit = 50) {
    return axios.get(`${API_BASE}/rankings/monthly`, {
      params: { year, month, limit }
    })
  },

  // 获取特定比赛排行
  getRankingByCompetition(competitionId) {
    return axios.get(`${API_BASE}/rankings/competition/${competitionId}`)
  },

  // 按日期范围获取排行
  getRankingByDateRange(startDate, endDate, limit = 50) {
    return axios.get(`${API_BASE}/rankings/date-range`, {
      params: { startDate, endDate, limit }
    })
  },

  // 获取运动员排名
  getAthleteRanking(athleteId) {
    return axios.get(`${API_BASE}/rankings/athlete/${athleteId}`)
  },

  // 获取运动员月度排名
  getAthleteMonthlyRanking(athleteId, year = null, month = null) {
    return axios.get(`${API_BASE}/rankings/athlete/${athleteId}/monthly`, {
      params: { year, month }
    })
  }
}

export const statisticsAPI = {
  // 获取月度训练统计
  getMonthlyTrainingStatistics(year = null, month = null) {
    return axios.get(`${API_BASE}/statistics/training/monthly`, {
      params: { year, month }
    })
  },

  // 获取月度比赛统计
  getMonthlyCompetitionStatistics(year = null, month = null) {
    return axios.get(`${API_BASE}/statistics/competition/monthly`, {
      params: { year, month }
    })
  },

  // 获取季度统计
  getQuarterlyStatistics(year = null, quarter = null, reportType = 'OVERALL') {
    return axios.get(`${API_BASE}/statistics/quarterly`, {
      params: { year, quarter, reportType }
    })
  },

  // 获取年度统计
  getYearlyStatistics(year = null, reportType = 'OVERALL') {
    return axios.get(`${API_BASE}/statistics/yearly`, {
      params: { year, reportType }
    })
  },

  // 获取自定义时间范围统计
  getCustomStatistics(startDate, endDate, reportType = 'OVERALL') {
    return axios.get(`${API_BASE}/statistics/custom`, {
      params: { startDate, endDate, reportType }
    })
  },

  // 获取运动员个人月度统计
  getAthleteMonthlyStatistics(athleteId, year = null, month = null) {
    return axios.get(`${API_BASE}/statistics/athlete/${athleteId}/monthly`, {
      params: { year, month }
    })
  },

  // 获取运动员个人年度统计
  getAthleteYearlyStatistics(athleteId, year = null) {
    return axios.get(`${API_BASE}/statistics/athlete/${athleteId}/yearly`, {
      params: { year }
    })
  }
}
