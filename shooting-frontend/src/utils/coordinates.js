export const TARGET_SIZE = 200;

/**
 * 判断坐标值是否处于归一化区间 [0, 1]
 * @param {number} value
 * @returns {boolean}
 */
const isNormalizedValue = (value) =>
  typeof value === 'number' && value >= 0 && value <= 1;

/**
 * 将单个坐标值转换为展示坐标
 * @param {number} value
 * @returns {number}
 */
const toDisplayValue = (value) =>
  isNormalizedValue(value) ? value * TARGET_SIZE : value;

/**
 * 返回带有展示坐标的新射击记录对象
 * @param {Object} record
 * @returns {Object}
 */
export const toDisplayRecord = (record = {}) => {
  if (
    typeof record !== 'object' ||
    record === null ||
    (record.x === undefined && record.y === undefined)
  ) {
    return record;
  }

  const displayX = toDisplayValue(record.x);
  const displayY = toDisplayValue(record.y);

  // 如果无需转换，则直接返回原始对象
  if (displayX === record.x && displayY === record.y) {
    return record;
  }

  return {
    ...record,
    x: displayX,
    y: displayY
  };
};

/**
 * 将一组射击记录转换为展示坐标
 * @param {Array<Object>} records
 * @returns {Array<Object>}
 */
export const toDisplayRecords = (records = []) =>
  Array.isArray(records) ? records.map((record) => toDisplayRecord(record)) : [];
