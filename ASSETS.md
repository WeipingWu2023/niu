# niu 美术资产

采用内置 imagegen；未使用 API/CLI 后备模式。参考用户提供的金黄色牛形截图，电影名称未作为事实核实或官方合作声明。

- `assets/icon-master.png`：金黄色牛形肖像，半垂眼、浅色宽鼻、斑点弯角、红衬衫；深酒红背景。手机图标由 Android 自适应图标遮罩，应用内采用圆角四边形。
- `assets/icon-rounded.png`：imagegen 精确编辑圆角四边形外轮廓，角外实际透明。192/512像素与旧式桌面图标由此仅缩放生成；自适应图标保留原始全幅画面。
- `assets/icon-192.png`、`assets/icon-512.png` 及 Android mipmap：原图仅作标准尺寸缩放，用于打包。
- `assets/poster.png`：最终 niu 竖版宣传海报。手机画面是生成示意，非真实截图，已明确标注。

## 生成提示

Icon: Create a premium Android app launcher icon using the user reference's golden anthropomorphic bull: relaxed half-lidded eyes, confident subtle smile, broad pale beige muzzle, upward mottled ivory horns, warm textured ochre fur, red collared shirt. Polished 3D animated movie aesthetic, oxblood red #9B2C31 background. Centered face readable at small sizes, horns inside safe region. No text, movie title, credits, chart, watermark or money symbol. Android applies the icon mask.

Poster: Finished vertical Chinese Android app poster, premium oxblood red and champagne gold, cream typography, matching golden bull, phone product mockup explicitly labelled 界面示意. Text: niu / 基金日记 / 把每一笔，记清楚。 / 暖红新装 · 安卓离线记账 / 确认前后，一眼看清 / 交易与日记，一起留下 / 本机保存，完整备份 / 免费下载安装 / GitHub · WeipingWu2023 / niu / 个人基金记录工具 · 非交易平台 · 不承诺投资收益. Mock holding value 1710.00, cost 2000.00, unrealized -290.00. No QR code or app-store endorsement.

Final edit prompt: Preserve the red/gold poster and bull, replace the title with large lowercase niu and smaller descriptor 基金日记, use niu as phone heading, change GitHub path to WeipingWu2023 / niu, replace the mock phone's 定投 label with 加仓. Preserve all other feature text and the non-promissory footer.

Rounded icon edit: Preserve the bull, expression, framing, red shirt and oxblood background. Change only the outer silhouette to a rounded square with 22% corner radius and genuine alpha transparency outside all four corners. No border, outside shadow, text, painted checkerboard or redesign. Keep horn tips inside the icon. Output square RGBA.
