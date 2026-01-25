<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>XJ-RULE | 产研共振 · 可视化决策中枢</title>
    <style>:root {
            --brand: #2563eb;
            --brand-soft: #eff6ff;
            --ai-glow: #8b5cf6;
            --text-h: #0f172a;
            --text-p: #475569;
            --bg-gray: #f8fafc;
            --success: #22c55e;
            --border-soft: #f1f5f9;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            background: #fff;
            font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
            color: var(--text-p);
            line-height: 1.6;
            overflow-x: hidden;
        }

        /* ---------------- 导航 ---------------- */
        nav {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 8%;
            position: fixed;
            width: 100%;
            top: 0;
            z-index: 1000;
            background: rgba(255, 255, 255, 0.85);
            backdrop-filter: blur(20px);
            border-bottom: 1px solid var(--border-soft);
        }

        .logo-container {
            display: flex;
            align-items: center;
            text-decoration: none;
        }

        .logo-icon {
            width: 36px;
            height: 36px;
            margin-right: 12px;
        }

        .logo-text {
            font-size: 1.5rem;
            font-weight: 800;
            color: var(--text-h);
            letter-spacing: -1px;
            display: flex;
            align-items: center;
        }

        .logo-text span {
            color: var(--brand);
        }

        .logo-text ai {
            color: var(--ai-glow);
            font-style: normal;
            margin-left: 2px;
        }

        .logo-text em {
            font-style: normal;
            font-size: 1rem;
            color: var(--text-h);
            margin-left: 12px;
            font-weight: 600;
            border-left: 1px solid #e2e8f0;
            padding-left: 12px;
        }

        .btn-brand {
            display: inline-block;
            padding: 14px 32px;
            background: var(--brand);
            color: #fff;
            text-decoration: none;
            border-radius: 10px;
            font-weight: 700;
            transition: 0.3s;
        }

        .btn-brand:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px rgba(37, 99, 235, 0.2);
        }

        /* ---------------- Hero ---------------- */
        .hero {
            padding: 180px 8% 100px;
            text-align: center;
            background: radial-gradient(circle at 50% 0%, #eff6ff 0%, #f5f3ff 30%, transparent 70%);
        }

        .badge {
            display: inline-block;
            padding: 6px 16px;
            background: #ede9fe;
            color: var(--ai-glow);
            border-radius: 30px;
            font-size: 0.85rem;
            font-weight: 600;
            margin-bottom: 24px;
            border: 1px solid #ddd6fe;
        }

        .hero h1 {
            font-size: 3.8rem;
            color: var(--text-h);
            line-height: 1.2;
            margin-bottom: 24px;
        }

        .hero h1 span {
            color: var(--brand);
        }

        .hero h1 i {
            color: var(--ai-glow);
            font-style: normal;
        }

        .hero p {
            font-size: 1.25rem;
            max-width: 900px;
            margin: 0 auto 40px;
            color: var(--text-p);
        }

        /* ---------------- 业务场景 ---------------- */
        .scenario-section {
            padding: 80px 8%;
            background: #fff;
        }

        .section-header {
            text-align: center;
            margin-bottom: 50px;
        }

        .section-header h2 {
            font-size: 2.5rem;
            color: var(--text-h);
            margin-bottom: 15px;
        }

        .example-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 24px;
        }

        .example-card {
            background: var(--bg-gray);
            border: 1px solid #eef2ff;
            border-radius: 24px;
            padding: 32px;
            transition: 0.3s;
        }

        .example-card:hover {
            border-color: var(--ai-glow);
            background: #fff;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
        }

        .canvas-demo {
            margin-top: 25px;
            background: #f1f5f9;
            padding: 20px;
            border-radius: 12px;
            border: 1px dashed #cbd5e1;
            min-height: 160px;
        }

        .node {
            background: #fff;
            padding: 10px 15px;
            border-radius: 6px;
            font-size: 0.85rem;
            margin-bottom: 10px;
            border-left: 4px solid var(--brand);
            display: flex;
            align-items: center;
            justify-content: space-between;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        }

        .node.ai {
            border-left-color: var(--ai-glow);
        }

        /* ---------------- 审计区域 (核心优化点) ---------------- */
        .audit-section {
            padding: 100px 8%;
            background: #0f172a;
            color: #fff;
            display: flex;
            align-items: center;
            gap: 80px;
        }

        .feature-list {
            list-style: none;
            padding: 0;
        }

        .feature-list li {
            margin-bottom: 22px;
            display: flex;
            align-items: flex-start;
            gap: 16px;
            font-size: 1.05rem;
            color: rgba(255, 255, 255, 0.9);
        }

        .icon-check-wrapper {
            width: 22px;
            height: 22px;
            background: rgba(34, 197, 94, 0.1);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            border: 1.5px solid rgba(34, 197, 94, 0.4);
            margin-top: 2px;
        }

        .icon-check-inner {
            width: 6px;
            height: 10px;
            border: solid #22c55e;
            border-width: 0 2px 2px 0;
            transform: rotate(45deg);
            margin-bottom: 2px;
        }

        .audit-ui {
            flex: 1;
            background: rgba(255, 255, 255, 0.03);
            padding: 30px;
            border-radius: 20px;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .status-tag {
            font-size: 0.7rem;
            padding: 2px 8px;
            border-radius: 4px;
            font-weight: 700;
            margin-left: 8px;
        }

        .tag-deployed {
            background: rgba(34, 197, 94, 0.2);
            color: #4ade80;
            border: 1px solid rgba(34, 197, 94, 0.3);
        }

        .tag-draft {
            background: rgba(148, 163, 184, 0.1);
            color: #94a3b8;
            border: 1px solid rgba(148, 163, 184, 0.3);
        }

        /* ---------------- 其他 ---------------- */
        .values {
            padding: 80px 8%;
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 30px;
        }

        .value-card {
            padding: 40px;
            border-radius: 24px;
            background: #fff;
            border: 1px solid var(--border-soft);
        }

        .cta-footer {
            padding: 120px 8%;
            text-align: center;
            background: linear-gradient(to bottom, #fff, #f5f3ff);
        }

        footer {
            text-align: center;
            padding: 40px;
            color: #94a3b8;
            font-size: 0.85rem;
            border-top: 1px solid var(--border-soft);
        }

        @media (max-width: 1024px) {
            .hero h1 {
                font-size: 2.8rem;
            }

            .example-grid, .values, .audit-section {
                grid-template-columns: 1fr;
                text-align: center;
            }

            .audit-section {
                gap: 40px;
            }
        }</style>
</head>
<body>

<nav>
    <a href="#" class="logo-container">
        <svg class="logo-icon" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <defs>
                <linearGradient id="center_glow" x1="18" y1="18" x2="30" y2="30" gradientUnits="userSpaceOnUse">
                    <stop stop-color="#2563eb"/>
                    <stop offset="1" stop-color="#8b5cf6"/>
                </linearGradient>
            </defs>
            <path d="M8 14C8 14 18 26 24 26C30 26 40 14 40 14" stroke="#2563eb" stroke-width="5" stroke-linecap="round"
                  stroke-linejoin="round"/>
            <path d="M12 38C12 38 20 26 24 26C28 26 36 38 36 38" stroke="#8b5cf6" stroke-width="5"
                  stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="24" cy="26" r="6" fill="url(#center_glow)" stroke="#fff" stroke-width="2"/>
        </svg>

        <div class="logo-text">XJ<span>-RULE</span><em>星极规则引擎</em></div>
    </a>

    <div style="display: flex; gap: 30px;">
        <a href="https://github.com/threefish/rule-engine" target="_blank"
           style="text-decoration:none; color:var(--ai-glow); font-weight:700;">GitHub</a>
    </div>
</nav>

<section class="hero">
    <div class="badge">技术与业务共振 · 释放生产力</div>
    <h1>让<span>确定性逻辑</span><br>遇见<i>创造性 AI</i></h1>
    <p>XJ-RULE 旨在连接技术底座与业务增长。通过直观的<b>可视化画布</b>编排严谨业务规则，并深度集成<b>生成式 AI</b>，实现复杂决策的分钟级落地与敏捷更迭。
    </p>
    <a href="/index.html" class="btn-brand" style="background: linear-gradient(135deg, var(--brand), var(--ai-glow));">开启可视化决策之旅
        &rarr;</a>
</section>

<section class="scenario-section">
    <div class="section-header">
        <h2>全场景图形化编排实例</h2>
        <p>在画布上，每一条连线都是业务增长的路径</p>
    </div>

    <div class="example-grid">
        <div class="example-card">
            <h4>🛡️ 确定性：智能风控过滤</h4>
            <p>业务专家在画布上通过逻辑组合，实时布防拦截策略。当黑产通过高频接口攻击时，实现秒级响应与路径熔断。</p>
            <div class="canvas-demo">
                <div class="node">📍 身份准入校验 <span style="color:var(--success);">OK</span></div>
                <div class="node">📍 单日金额阈值判定 <span style="color:#ef4444;">FAIL</span></div>
                <div class="node" style="background:#fee2e2;">🚫 执行动作：拦截并记录日志</div>
            </div>
        </div>

        <div class="example-card">
            <h4>✨ 创造性：AI 个性化营销</h4>
            <p>在营销流中植入 AI 节点。根据用户画像自动编排 Prompt，实时产出极具温度的文字与定制化视觉海报。</p>
            <div class="canvas-demo">
                <div class="node">📍 人群标签识别：摄影爱好者</div>
                <div class="node ai">🔮 AI 节点：生成拟人化周年祝福</div>
                <div class="node ai">🎨 AI 节点：渲染徕卡风格海报</div>
            </div>
        </div>

        <div class="example-card">
            <h4>⚡ 敏捷性：促销策略秒级更迭</h4>
            <p>不再受限于发版排期。运营人员直接在画布修改优惠权重或满减节点，通过一键热部署，策略即刻覆盖生产环境。</p>
            <div class="canvas-demo">
                <div class="node" style="text-decoration: line-through; opacity: 0.5;">📍 满 300 减 40 (旧逻辑)</div>
                <div class="node">📍 规则准入：双11大促活动波段</div>
                <div class="node" style="border: 1px solid var(--success); color: var(--success);">📍 满 299 减 50
                    (画布实时生效)
                </div>
            </div>
        </div>

        <div class="example-card">
            <h4>🔗 协同性：全链路可视化审计</h4>
            <p>
                将复杂的业务逻辑转化为可感知的图形资产。每一个执行节点、每一个变量快照都清晰记录，让产研共用同一张“逻辑蓝图”。</p>
            <div class="canvas-demo">
                <div class="node">📍 实时输入：决策上下文快照</div>
                <div class="node">📍 逻辑调度：权益分流路由</div>
                <div class="node">📍 决策输出：下发最优优惠指令</div>
            </div>
        </div>
    </div>
</section>


<section class="values">
    <div class="value-card">
        <h3>🧩 画布即文档</h3>
        <p>打破产研沟通壁垒。可视化画布既是运行逻辑，也是最直观的业务文档，实现技术与业务的深度对齐。</p>
    </div>
    <div class="value-card">
        <h3>🔮 AI 节点深度集成</h3>
        <p>将 LLM 转化为画布上的可编排节点。让业务逻辑不再局限于判断，而是具备理解与创造的能力。</p>
    </div>
    <div class="value-card">
        <h3>🛡️ 全路径执行追溯</h3>
        <p>拒绝决策黑盒。每一个执行节点的输出、耗时、Prompt 详情均可审计回溯，让系统既敏捷又稳健。</p>
    </div>
</section>


<section class="audit-section">
    <div style="flex: 1.2;">
        <h2 style="font-size: 2.4rem; margin-bottom: 24px; letter-spacing: -1px; background: linear-gradient(to right, #fff, #94a3b8); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">
            让技术深耕，支撑业务领航</h2>
        <p style="opacity: 0.7; font-size: 1.1rem; margin-bottom: 35px; line-height: 1.8; font-weight: 300;">
            在 XJ-RULE 中，每一项决策都是透明的。我们不仅提供极致的编排体验，更确保全链路的审计与可控，让敏捷与稳健并行。
        </p>
        <ul class="feature-list">
            <li>
                <div class="icon-check-wrapper">
                    <div class="icon-check-inner"></div>
                </div>
                <span><b>双态流转：</b>独有的“草稿-部署”模式，支持逻辑热发布与秒级生效。</span>
            </li>
            <li>
                <div class="icon-check-wrapper">
                    <div class="icon-check-inner"></div>
                </div>
                <span><b>路径审计：</b>全量记录每一个节点的输入输出，让每一笔决策都可回溯。</span>
            </li>
            <li>
                <div class="icon-check-wrapper">
                    <div class="icon-check-inner"></div>
                </div>
                <span><b>性能监控：</b>深度透视 IO 节点耗时，拒绝执行黑盒。</span>
            </li>
        </ul>
    </div>
    <div class="audit-ui">
        <div style="font-family: 'Fira Code', monospace; font-size: 0.85rem; color: #94a3b8;">
            <div style="display:flex; justify-content:space-between; margin-bottom:20px; border-bottom:1px solid rgba(255,255,255,0.05); padding-bottom:10px;">
                <span style="color:#64748b;">ENGINE_STATUS: <b>ACTIVE</b></span>
                <span>
                    <span class="status-tag tag-deployed">● DEPLOYED</span>
                    <span class="status-tag tag-draft">○ DRAFT</span>
                </span>
            </div>
            <p style="margin-bottom: 8px;"><b style="color:#e2e8f0;">[Trace: 风控过滤]</b> 响应：<span
                        style="color:var(--success);">PASS</span></p>
            <p style="margin-bottom: 8px;"><b style="color:#e2e8f0;">[Node: AI 决策]</b> 耗时：<span
                        style="color:var(--ai-glow);">124ms</span></p>
            <p style="margin-bottom: 8px;"><b style="color:#e2e8f0;">[Context: 用户]</b> ID: <span
                        style="color:#38bdf8;">U_8821</span></p>
            <div style="margin-top:20px; padding:12px; background:rgba(0,0,0,0.2); border-radius:8px; border-left:3px solid var(--brand);">
                <span style="color:#64748b; font-size:0.75rem;">TRACE_ID: xjr_7721_prod | CALLER_ID: Biz_99</span>
            </div>
        </div>
    </div>
</section>


<section class="cta-footer">
    <h2 style="font-size: 2.5rem; color: var(--text-h); margin-bottom: 24px;">技术与业务共振，释放数字化生产力</h2>
    <p style="margin-bottom: 40px; font-size: 1.1rem; color: var(--text-p);">
        立即使用 XJ-RULE，体验由“严谨逻辑”与“灵动 AI”共同驱动的敏捷决策新纪元。
    </p>
    <a href="/index.html" class="btn-brand"
       style="padding: 20px 60px; font-size: 1.2rem; background: linear-gradient(135deg, var(--brand), var(--ai-glow));">立即开启智能编排</a>
</section>

<footer>
    <p>&copy; 2026 <a href="https://github.com/threefish" style="text-decoration:none; color:inherit;">星极技术团队
            (Threefish)</a> · 数字化决策与 AI 编排领航者</p>
    <p style="margin-top: 10px; opacity: 0.5;">产研协同 · 可视化编排 · 全链路透明审计</p>
</footer>

</body>
</html>