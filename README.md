# Pokémon GO Special Events

Pokémon GO 特殊活動資料庫與查詢網站。

## V2 核心規則
- 每次只處理使用者當次提供的網址、文字或截圖；未特別要求不外部補查。
- 來源未提供的時間、地點或 GPS 不猜測，以狀態欄位標記。
- 來源沒有可形成完整日期的活動日期時，以資料處理日為 startDate、+6 天為 endDate（共 7 天）暫定入庫，標記 `timeStatus: estimated`；日後取得正式日期時覆蓋。
- 同一活動沿用穩定 id，後續資訊更新原資料，不重複新增。
- 一活動可含多個 locations；第一個為 primary，其餘為 additional。
- 特殊獲取物統一放 specialRewards。
- createdAt 保留首次建立日期；updatedAt 記錄最後更新日期。

## 更新流程
使用者提供資料 → 完整讀取當次來源 → 判斷新/既有活動 → 擷取資料 → 驗證 → 更新 data/events.json → GitHub Pages 自動發布。

## 檔案
- data/events.json：V2 活動資料
- data/schema.json：V2 Schema
- docs/UPDATE_WORKFLOW.md：更新規範
- index.html / event.html：網站
