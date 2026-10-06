# 涉密载体按季对册——规矩落点对照（chen_025）

市保密办今年口径在代码里的落点。全系统只有两处判路，旁处不许再留第二处。

## 一、「还差几件」唯一判路

- 唯一算法：`com.fc.v2.service.secre.CarrierLedger#lack(need, got)`，就是**应交 − 已收**。
  - 应交 30 / 已收 12 → 18；应交 20 / 已收 7 → 13；两数齐平 → 0。
- 立账校验：`CarrierLedger#validate`（新增入册、改已收件数、换挂靠名录都先过它）。
  - 应交空着 / 负数 / 小于已收、已收为负 / 冒过应交 → 抛 `SecreLedgerException`，按栏位点名
    （`needNum` / `gotNum`），全局异常处理把 `field` 一并回给页面；只丢「数不对劲」不算交代。
  - 名录代号对不上册面 → 点 `bookNo` 栏。
- 报文里的 `lackNum` 一律不作数：`insertTSecreCarrier` 先置空，落库前由本行两个数重轧；
  `updateTSecreCarrier` 改口后同样重轧。页面读数（list/get）再逐行用同一函数回轧一遍，
  「页面摆出来的数」与「回库逐行点出来的数」同处算账。
- 单测：`src/test/java/com/fc/v2/service/secre/CarrierLedgerTest.java`（8 条）。

## 二、行与名录只走代号文本

- 挂接一律用 `book_no`（文本）查名录；`book_id` 主键列保留但弃用，新增/改口强制置空。
- 名录查询统一走 `ITSecreOrgBookService#selectByBookNo`。

## 三、名录撤出：只挡新行，不抹旧账

- 入新行/改口挂到 `status=1（已撤出）` 名录 → 点 `bookNo` 打回。
- 撤出前挂在它名下的旧行：列表照常列、视野内照常算（视野捞名录时不剔除已撤出）。

## 四、上级下发 vs 本机关自录：本地为准、冲突点名

- 名录可同代号存两份，`sync_kind` 区分「上级名录下发」「本机关自录」；作准取自录那份。
- 每日对账入口：`POST /SecreOrgBookController/reconcileUpper`（报文=上级清单）：
  - 上级有、本地无 → 落库（来路记上级下发）；
  - 同代号全称或在册情形对不上 → **本地一字不动**（上级说已撤号也不动），
    逐条点代号写 `t_secre_org_sync_conflict`，缘由同时追加到本地名录 `remark`；
    响应 data 直接回冲突清单，绝不悄悄盖掉。
  - 查询：`GET /SecreOrgBookController/conflicts`。

## 五、按知悉范围划视野（唯一判路）

- 配置表：`t_secre_user_scope`（屏别 0 本级经办岗 / 1 市保密办），
  市办另由 `t_secre_scope_book` 点名划到的名录代号。
- 判路：`com.fc.v2.service.secre.CarrierScopeService#currentView()`。
  - 本级经办岗：按 `road_name`（一直写到街道）把作准名录（含已撤出）整串捞出，只见这些行；
  - 市保密办：三层可见，但只看见点名划给它的那几家；
  - 没划范围 / 本季无行 → 空屏（查询强制 `1=0`，不借邻家行）。
- list、逐行 get、入册挂名录、改口、删除五处共用此判路。
- 对册屏：`GET /SecreCarrierController/screen?quarter=2026Q3`（留空当季，all 不按季）；
  件码精确回查不按季截断——库里入过的每一行本屏都寻得着。

## 六、入口

- 原有 `/SecreCarrierController/add`、`/edit`、`/remove`、`/list` 收什么回什么维持原状；
  另给同义入册姿势 `POST /SecreCarrierController/register`（同一判路、同一轧账）。
- 页面：`templates/admin/secreCarrier/list.html`（对册屏，差额只读展示）、`add.html`、`edit.html`。
- 菜单/权限/知悉范围种子见 `doc/schema/secre.sql` 末尾。
