# Collector Inbox

此目錄是 Android Collector 與正式活動資料之間的隔離層。

- incoming/: 手機同步後的待處理 metadata
- processed/: 完成 V2 匯入後的紀錄
- attachments 不直接寫入 events.json
- Collector 不得直接修改 data/events.json

## 安全原則
APK 不保存 GitHub PAT。手機只可呼叫受控 HTTPS Inbox API。
API 驗證通過後才可建立 incoming item。
正式 events.json 仍由 V2 驗證流程更新。
