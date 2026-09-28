# Pokémon GO Special Events

私人 Pokémon GO 特殊活動資料庫。

## 原則
- 新活動只依使用者當次提供的網頁、文字或截圖整理。
- 未特別要求時，不主動從其他網站或其他 ChatGPT 對話補資料。
- 來源沒有提供的資料標示為未提供，不自行推測。
- 一個活動一筆資料；多個 GPS 存在同一活動的 locations 陣列。
- specialRewards 用分類資料保存，方便網站篩選。

## 結構
- `data/events.json`：活動資料索引
- `data/schema.json`：活動資料格式
- `public/`：網站
- `firebase.json`：Firebase Hosting 設定
