-- secre 涉密载体入册清退与密级变更保密监管 -- schema (chen-025)
-- 列名与基线实体契约（@TableName/@TableField）逐列对齐，改列必须同步实体。
-- 库：chen_025

CREATE TABLE IF NOT EXISTS t_secre_carrier (
  id bigint NOT NULL COMMENT '主键',
  carrier_no varchar(64) DEFAULT NULL COMMENT '涉密载体件码',
  book_id int DEFAULT NULL COMMENT '挂在哪一册名录名下',
  book_no varchar(64) DEFAULT NULL COMMENT '名录代号（冗余自名录）',
  post_no varchar(64) DEFAULT NULL COMMENT '使用岗或承办岗代号',
  need_num int DEFAULT NULL COMMENT '应交件数',
  got_num int DEFAULT NULL COMMENT '已收件数',
  lack_num int DEFAULT NULL COMMENT '还差几件（轧出来，不手填）',
  content varchar(255) DEFAULT NULL COMMENT '随件交来的载体题名与要件',
  status int DEFAULT NULL COMMENT '册面情形 0新入册 1已收齐 2缺项',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='涉密载体册面';

CREATE TABLE IF NOT EXISTS t_secre_disp_flow (
  id bigint NOT NULL COMMENT '主键',
  biz_no varchar(64) DEFAULT NULL COMMENT '载体处置流转单号',
  stage int DEFAULT NULL COMMENT '当前关次 0..3（核件/定级/清退/监销）',
  status int DEFAULT NULL COMMENT '单子走到哪一关 0未起 1在办 2已封住',
  content varchar(255) DEFAULT NULL COMMENT '一关一记',
  last_action varchar(64) DEFAULT NULL COMMENT '最近一次挪关动作',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='载体处置流转单';

CREATE TABLE IF NOT EXISTS t_secre_hand_row (
  id bigint NOT NULL COMMENT '主键',
  batch_no varchar(64) DEFAULT NULL COMMENT '清退移交批次码',
  row_no int DEFAULT NULL COMMENT '原报行次',
  item_code varchar(64) DEFAULT NULL COMMENT '被对上的载体件码',
  qty decimal(12,2) DEFAULT NULL COMMENT '本行应交件数',
  quali_num decimal(12,2) DEFAULT NULL COMMENT '本行核收合格件数',
  hand_kind varchar(32) DEFAULT NULL COMMENT '交回渠道（本级送交／上级调回两条来路）',
  status int DEFAULT NULL COMMENT '行落地情形 0未勾 1已收下 2已退回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='清退移交行';

CREATE TABLE IF NOT EXISTS t_secre_level_bill (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '密级变更签批单',
  node_no int DEFAULT NULL COMMENT '当前所在关 0..2（承办部门/本机关保密办/上级主管部门）',
  sign_mode int DEFAULT NULL COMMENT '本关算齐办法 0一名即过 1两关须同判',
  need_count int DEFAULT NULL COMMENT '本关应签人数',
  sign_count int DEFAULT NULL COMMENT '本关已签人数',
  status int DEFAULT NULL COMMENT '签批情形 0在签 1已点齐 2已压回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='密级变更签批单';

CREATE TABLE IF NOT EXISTS t_secre_org_book (
  id bigint NOT NULL COMMENT '主键',
  book_no varchar(64) DEFAULT NULL COMMENT '机构名录代号',
  book_name varchar(128) DEFAULT NULL COMMENT '单位全称',
  book_kind varchar(32) DEFAULT NULL COMMENT '单位类别',
  road_name varchar(128) DEFAULT NULL COMMENT '属地一直写到街道（浔州省—谷城市—区—街道）',
  sync_kind varchar(32) DEFAULT NULL COMMENT '来路（上级名录下发／本机关自录）',
  status int DEFAULT NULL COMMENT '名录情形 0在册 1已撤出',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机构名录底册';

CREATE TABLE IF NOT EXISTS t_secre_urge_item (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '届期催收与巡查条目代号',
  due_at datetime DEFAULT NULL COMMENT '该交回那一日的止点时刻',
  amount decimal(12,2) DEFAULT NULL COMMENT '最多许提前几日',
  content varchar(255) DEFAULT NULL COMMENT '事由与所对载体的记要',
  status int DEFAULT NULL COMMENT '条目情形 0搁着 1已派出 2派不成',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='届期催收与巡查条目';

CREATE TABLE IF NOT EXISTS t_secre_warn_line (
  id bigint NOT NULL COMMENT '主键',
  rule_code varchar(64) DEFAULT NULL COMMENT '届期预警分级线代号',
  rule_name varchar(128) DEFAULT NULL COMMENT '线名',
  obj_kind varchar(32) DEFAULT NULL COMMENT '这条线管载体册面还是签批卷面',
  th1_max decimal(12,3) DEFAULT NULL COMMENT '起算日数',
  th2_max decimal(12,3) DEFAULT NULL COMMENT '够线日数',
  th3_max decimal(12,3) DEFAULT NULL COMMENT '封顶日数',
  eff_start datetime DEFAULT NULL COMMENT '起用那一日',
  eff_end datetime DEFAULT NULL COMMENT '交班那一日(不含)',
  priority int DEFAULT NULL COMMENT '交班次序(数值大的先说话)',
  status int DEFAULT NULL COMMENT '线的情形 0在位 1已撤下',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='届期预警分级线';

-- 知悉范围配置：把一屏划给某个用户（scope_type 0=本级经办岗 1=市保密办）。
-- 本级经办岗按属地（road_name 一直写到街道）见行；市保密办按 t_secre_scope_book 点名划给它的那几家。
CREATE TABLE IF NOT EXISTS t_secre_user_scope (
  id bigint NOT NULL COMMENT '主键',
  user_id bigint DEFAULT NULL COMMENT '划给谁（t_sys_user.id）',
  scope_type int DEFAULT 0 COMMENT '屏别 0本级经办岗 1市保密办',
  road_name varchar(128) DEFAULT NULL COMMENT '本级属地（一直写到街道，本级经办岗按此见行）',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='涉密载体知悉范围配置';

-- 市保密办那一屏被点名划到的名录代号（只看得见这几家；本级经办岗不用此表，按属地见行）
CREATE TABLE IF NOT EXISTS t_secre_scope_book (
  id bigint NOT NULL COMMENT '主键',
  scope_id bigint DEFAULT NULL COMMENT '知悉范围配置id',
  book_no varchar(64) DEFAULT NULL COMMENT '划给它的名录代号（走代号文本，不走主键）',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_scope_book_scope (scope_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知悉范围点名名录';

-- 上级下发名录与本机关自录名录的冲突台账：同代号两份对不上时逐条点名，不许悄悄盖掉
CREATE TABLE IF NOT EXISTS t_secre_org_sync_conflict (
  id bigint NOT NULL COMMENT '主键',
  book_no varchar(64) DEFAULT NULL COMMENT '起冲突的名录代号',
  upper_book_name varchar(128) DEFAULT NULL COMMENT '上级那份的单位全称',
  local_book_name varchar(128) DEFAULT NULL COMMENT '本机关自录那份的单位全称',
  upper_status int DEFAULT NULL COMMENT '上级那份名录情形 0在册 1已撤号',
  local_status int DEFAULT NULL COMMENT '本机关那份名录情形 0在册 1已撤出',
  reason varchar(500) DEFAULT NULL COMMENT '冲突缘由与裁定（以本机关自录为准）',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_org_conflict_no (book_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='名录上下发冲突台账';

-- 初始档案数据（id=1 启用 / id=2 停用）
-- t_secre_org_book 两条种子名录：book_id=0 在册、book_id=1 已撤出（供后续挂接与挡新入册
-- 判定项取用）。名录代号与类别按「三类各排各的序」的说法给值。
INSERT IGNORE INTO t_secre_org_book (id, book_no, book_name, book_kind, road_name, status, del_flag, create_by, create_time)
VALUES (0, 'ML00', '浔州区谷城街道·中共谷城市委机关（机关类，在册）', '机关', '浔州省—谷城市—浔州区—谷城街道', 0, 0, 'seed', NOW()),
       (1, 'ML01', '北塔区岭南区·市测绘院下属资料室（已并走撤出）', '事业单位', '浔州省—谷城市—北塔区—岭南街道', 1, 0, 'seed', NOW());

-- 知悉范围种子：admin 划市保密办一屏（点名 ML00/ML01，撤出的 ML01 旧账照样列得出来）；
-- fuce 划本级经办岗一屏，属地到谷城街道，只看得见挂在本街道名下行。
INSERT IGNORE INTO t_secre_user_scope (id, user_id, scope_type, road_name, del_flag, create_by, create_time)
VALUES (100, 1, 1, NULL, 0, 'seed', NOW()),
       (101, 433236479427350528, 0, '浔州省—谷城市—浔州区—谷城街道', 0, 'seed', NOW());

INSERT IGNORE INTO t_secre_scope_book (id, scope_id, book_no, del_flag, create_by, create_time)
VALUES (200, 100, 'ML00', 0, 'seed', NOW()),
       (201, 100, 'ML01', 0, 'seed', NOW());


-- 菜单与权限种子（挂在系统管理 592059865673760768 下；INSERT IGNORE，已存在不动）
INSERT IGNORE INTO t_sys_permission (id, name, descripion, url, is_blank, pid, perms, type, icon, order_num, visible, create_by, create_time)
VALUES (9001, '涉密载体按季对册', '涉密载体册面对册屏', '/SecreCarrierController/view', 0, 592059865673760768, 'secre:secreCarrier:view', 1, 'layui-icon layui-icon-list', 50, 0, 'seed', NOW()),
       (9002, '涉密载体集合', '按季+知悉范围列行', '/SecreCarrierController/screen', 0, 9001, 'secre:secreCarrier:list', 2, '', 50, 0, 'seed', NOW()),
       (9003, '涉密载体入册', '入册（差额当场轧）', '/SecreCarrierController/add', 0, 9001, 'secre:secreCarrier:add', 2, 'layui-icon layui-icon-add-1', 50, 0, 'seed', NOW()),
       (9004, '涉密载体改口', '已收/换名录改口重轧', '/SecreCarrierController/edit', 0, 9001, 'secre:secreCarrier:edit', 2, 'layui-icon layui-icon-edit', 50, 0, 'seed', NOW()),
       (9005, '涉密载体删除', '删除本屏行', '/SecreCarrierController/remove', 0, 9001, 'secre:secreCarrier:remove', 2, 'layui-icon layui-icon-delete', 50, 0, 'seed', NOW()),
       (9011, '机构名录底册', '名录自录与上级对账', '/SecreOrgBookController/list', 0, 592059865673760768, 'secre:secreOrgBook:list', 1, 'layui-icon layui-icon-template-1', 51, 0, 'seed', NOW()),
       (9012, '机构名录自录', '本机关自录名录', '/SecreOrgBookController/saveLocal', 0, 9011, 'secre:secreOrgBook:edit', 2, '', 51, 0, 'seed', NOW()),
       (9013, '上级名录对账', '每日上级下发对账，冲突点代号', '/SecreOrgBookController/reconcileUpper', 0, 9011, 'secre:secreOrgBook:sync', 2, '', 51, 0, 'seed', NOW());

-- 划给管理员角色（488243256161730560）
INSERT IGNORE INTO t_sys_permission_role (id, role_id, permission_id, create_by, create_time)
VALUES (9101, 488243256161730560, 9001, 'seed', NOW()),
       (9102, 488243256161730560, 9002, 'seed', NOW()),
       (9103, 488243256161730560, 9003, 'seed', NOW()),
       (9104, 488243256161730560, 9004, 'seed', NOW()),
       (9105, 488243256161730560, 9005, 'seed', NOW()),
       (9111, 488243256161730560, 9011, 'seed', NOW()),
       (9112, 488243256161730560, 9012, 'seed', NOW()),
       (9113, 488243256161730560, 9013, 'seed', NOW());
