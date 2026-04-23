import fs from 'node:fs';
import path from 'node:path';
import PDFDocument from 'pdfkit';

const workspaceRoot = process.cwd();
const outputDir = path.join(workspaceRoot, 'docs', 'diagrams');
const outputPath = path.join(outputDir, 'web-click-to-screen-sequence.pdf');
const regularFontPath = 'C:\\Windows\\Fonts\\arial.ttf';
const boldFontPath = 'C:\\Windows\\Fonts\\arialbd.ttf';

fs.mkdirSync(outputDir, { recursive: true });

if (!fs.existsSync(regularFontPath) || !fs.existsSync(boldFontPath)) {
  throw new Error('Arial fonts were not found in C:\\Windows\\Fonts.');
}

const doc = new PDFDocument({
  size: [1400, 980],
  margin: 30,
  compress: true,
});

doc.pipe(fs.createWriteStream(outputPath));
doc.registerFont('AppRegular', regularFontPath);
doc.registerFont('AppBold', boldFontPath);

const colors = {
  headerFill: '#5b7dee',
  headerStroke: '#4b6ad1',
  message: '#f0a500',
  response: '#5f86d6',
  divider: '#c7c7c7',
  text: '#222222',
  muted: '#666666',
  lane: '#f3f5f9',
};

const columns = [
  { key: 'user', title: 'Пользователь' },
  { key: 'ui', title: 'Angular UI' },
  { key: 'feature', title: 'Feature Layer' },
  { key: 'bridge', title: 'KmpBridgeService' },
  { key: 'webBridge', title: 'WebRootBridge' },
  { key: 'shared', title: 'Shared KMP' },
  { key: 'data', title: 'Data / Backend' },
];

const left = 40;
const right = 1360;
const top = 60;
const bottom = 930;
const laneWidth = (right - left) / columns.length;
const centers = new Map();

function drawPageTitle(title, subtitle) {
  doc.fillColor(colors.text).font('AppBold').fontSize(24).text(title, 40, 20);
  if (subtitle) {
    doc
      .fillColor(colors.muted)
      .font('AppRegular')
      .fontSize(12)
      .text(subtitle, 40, 48, { width: 1100 });
  }
}

function drawRoundedHeader(x, y, w, h, title) {
  doc
    .save()
    .roundedRect(x, y, w, h, 8)
    .fillAndStroke(colors.headerFill, colors.headerStroke)
    .restore();

  doc
    .fillColor('#ffffff')
    .font('AppBold')
    .fontSize(16)
    .text(title, x, y + 14, { width: w, align: 'center' });
}

function drawLane(index, title) {
  const x = left + index * laneWidth;
  const centerX = x + laneWidth / 2;
  centers.set(columns[index].key, centerX);

  doc
    .save()
    .roundedRect(x + 5, 110, laneWidth - 10, 770, 6)
    .fillAndStroke(colors.lane, '#d9dfea')
    .restore();

  drawRoundedHeader(x + 18, 122, laneWidth - 36, 38, title);

  doc
    .save()
    .lineWidth(3)
    .dash(10, { space: 6 })
    .strokeColor(colors.response)
    .moveTo(centerX, 175)
    .lineTo(centerX, 855)
    .stroke()
    .restore();
}

function drawUserActor(centerX) {
  doc.save();
  doc.circle(centerX, 210, 16).fillAndStroke('#9ec1ff', '#4b6ad1');
  doc.lineWidth(2);
  doc.moveTo(centerX, 226).lineTo(centerX, 258).stroke('#4b6ad1');
  doc.moveTo(centerX - 18, 238).lineTo(centerX + 18, 238).stroke('#4b6ad1');
  doc.moveTo(centerX, 258).lineTo(centerX - 16, 284).stroke('#4b6ad1');
  doc.moveTo(centerX, 258).lineTo(centerX + 16, 284).stroke('#4b6ad1');
  doc.restore();
}

function drawArrow(x1, y1, x2, y2, color, dashed = false) {
  const arrow = 8;
  const direction = x2 > x1 ? 1 : -1;

  doc.save();
  doc.lineWidth(2);
  doc.strokeColor(color);
  if (dashed) {
    doc.dash(7, { space: 4 });
  }
  doc.moveTo(x1, y1).lineTo(x2, y2).stroke();
  doc.undash();
  doc
    .moveTo(x2, y2)
    .lineTo(x2 - direction * arrow, y2 - arrow / 2)
    .lineTo(x2 - direction * arrow, y2 + arrow / 2)
    .closePath()
    .fill(color);
  doc.restore();
}

function drawMessage(fromKey, toKey, y, label, options = {}) {
  const fromX = centers.get(fromKey);
  const toX = centers.get(toKey);
  const textWidth = Math.abs(toX - fromX) - 30;
  const dashed = options.dashed ?? false;
  const color = options.color ?? (dashed ? colors.response : colors.message);

  drawArrow(fromX, y, toX, y, color, dashed);

  doc
    .fillColor(colors.text)
    .font(options.bold ? 'AppBold' : 'AppRegular')
    .fontSize(13)
    .text(label, Math.min(fromX, toX) + 12, y - 18, {
      width: textWidth,
      align: 'center',
    });
}

function drawSelfMessage(key, y, label) {
  const x = centers.get(key);
  const rightX = x + 55;

  doc.save().lineWidth(2).strokeColor(colors.message);
  doc.moveTo(x, y).lineTo(rightX, y).lineTo(rightX, y + 28).lineTo(x + 6, y + 28).stroke();
  doc
    .moveTo(x + 6, y + 28)
    .lineTo(x + 16, y + 23)
    .lineTo(x + 16, y + 33)
    .closePath()
    .fill(colors.message);
  doc.restore();

  doc
    .fillColor(colors.text)
    .font('AppRegular')
    .fontSize(12)
    .text(label, x + 18, y + 6, { width: 90, align: 'left' });
}

function drawSection(y, title) {
  doc
    .save()
    .lineWidth(1)
    .strokeColor(colors.divider)
    .moveTo(left, y)
    .lineTo(right, y)
    .stroke()
    .restore();

  doc
    .fillColor(colors.muted)
    .font('AppBold')
    .fontSize(14)
    .text(title, left + 8, y - 14, { width: 240 });
}

function drawScenarioPage(title, subtitle) {
  centers.clear();
  drawPageTitle(title, subtitle);
  columns.forEach((column, index) => drawLane(index, column.title));
  drawUserActor(centers.get('user'));
}

drawScenarioPage(
  'Схема взаимодействия слоев: сценарий 1',
  'Авторизация. Команды идут вправо по слоям, состояние и навигация возвращаются обратно на экран.',
);

drawSection(305, 'Сценарий 1. Авторизация');
drawMessage('user', 'ui', 340, '1. Нажать "Войти"');
drawMessage('ui', 'feature', 380, '2. click handler в FeatureAuth');
drawMessage('feature', 'bridge', 420, '3. authSubmit()');
drawMessage('bridge', 'webBridge', 460, '4. authSubmit()');
drawMessage('webBridge', 'shared', 500, '5. AuthComponent.submit()');
drawMessage('shared', 'data', 540, '6. LoginUseCase -> Repository -> REST');
drawSelfMessage('data', 565, 'проверка логина');
drawMessage('data', 'shared', 610, '7. session / profile / result', { dashed: true });
drawMessage('shared', 'webBridge', 650, '8. новое AuthState / RootChildKind', { dashed: true });
drawMessage('webBridge', 'bridge', 690, '9. authStateJson() + watchRootChildKind()', { dashed: true });
drawMessage('bridge', 'feature', 730, '10. signal.set(authState)', { dashed: true });
drawMessage('bridge', 'ui', 770, '11. rootChildKind -> Router.navigate(contacts)', { dashed: true });
drawMessage('ui', 'user', 810, '12. Экран контактов открыт', { dashed: true });

doc.addPage({ size: [1400, 980], margin: 30 });

drawScenarioPage(
  'Схема взаимодействия слоев: сценарий 2',
  'Открытие карточки контакта. Поток идет от клика в списке до получения ContactInfoState и рендера данных.',
);

drawSection(305, 'Сценарий 2. Открытие карточки контакта');
drawMessage('user', 'ui', 340, '1. Клик по контакту');
drawMessage('ui', 'feature', 380, '2. openInfo(index)');
drawMessage('feature', 'bridge', 420, '3. contactsOpenInfo(index)');
drawMessage('bridge', 'webBridge', 460, '4. contactsOpenInfo(index)');
drawMessage('webBridge', 'shared', 500, '5. ContactsComponent.openInfo(index)');
drawMessage('shared', 'data', 540, '6. получить детали / presence / cache');
drawMessage('data', 'shared', 580, '7. ContactInfoState', { dashed: true });
drawMessage('shared', 'webBridge', 620, '8. currentInfoComponentOrNull() + watchState()', { dashed: true });
drawMessage('webBridge', 'bridge', 660, '9. contactInfoStateJson()', { dashed: true });
drawMessage('bridge', 'feature', 700, '10. signal.set(contactInfoState)', { dashed: true });
drawMessage('feature', 'ui', 740, '11. Шаблон перерисован', { dashed: true });
drawMessage('ui', 'user', 780, '12. На экране данные контакта', { dashed: true });

doc.addPage({ size: [1400, 980], margin: 30 });

doc.fillColor(colors.text).font('AppBold').fontSize(22).text(
  'Что проходит между слоями',
  40,
  30,
);

const cards = [
  {
    x: 50,
    y: 90,
    w: 390,
    h: 200,
    title: 'UI -> Angular Feature',
    lines: [
      'DOM event: click / input / select',
      'Индекс контакта, текст поля, команда пользователя',
      'Никакой бизнес-логики, только намерение пользователя',
    ],
  },
  {
    x: 500,
    y: 90,
    w: 390,
    h: 200,
    title: 'Feature -> KmpBridgeService',
    lines: [
      'Типизированный вызов фасада',
      'Например: authSubmit(), contactsOpenInfo(index)',
      'На этом уровне UI отделен от KMP API',
    ],
  },
  {
    x: 950,
    y: 90,
    w: 390,
    h: 200,
    title: 'KmpBridgeService -> WebRootBridge',
    lines: [
      'Вызов JS bridge методов',
      'Подписки: watchAuthState, watchContactsListState, watchContactInfoState',
      'JSON parsing + обновление Angular signals',
    ],
  },
  {
    x: 50,
    y: 330,
    w: 390,
    h: 220,
    title: 'WebRootBridge -> Shared KMP',
    lines: [
      'Маршрутизация к RootComponent / AuthComponent / ContactsComponent',
      'currentState() / watchState() / watchChildKind()',
      'Сериализация состояния экрана в JSON',
    ],
  },
  {
    x: 500,
    y: 330,
    w: 390,
    h: 220,
    title: 'Shared KMP -> Data',
    lines: [
      'UseCase -> Repository -> Remote / SQLDelight / WebSocket',
      'Здесь живут бизнес-правила, навигация и построение состояния',
      'Результат: новый экранный state или ошибка',
    ],
  },
  {
    x: 950,
    y: 330,
    w: 390,
    h: 220,
    title: 'Обратный путь на экран',
    lines: [
      'Backend result -> KMP state',
      'KMP state -> JSON bridge -> Angular signals',
      'Signals / Router -> template -> информация на экране',
    ],
  },
];

for (const card of cards) {
  doc
    .save()
    .roundedRect(card.x, card.y, card.w, card.h, 10)
    .fillAndStroke('#ffffff', '#b7c0d8')
    .restore();

  doc
    .save()
    .roundedRect(card.x + 14, card.y + 14, card.w - 28, 34, 8)
    .fillAndStroke(colors.headerFill, colors.headerStroke)
    .restore();

  doc
    .fillColor('#ffffff')
    .font('AppBold')
    .fontSize(15)
    .text(card.title, card.x + 24, card.y + 24, { width: card.w - 48, align: 'center' });

  let lineY = card.y + 72;
  for (const line of card.lines) {
    doc
      .fillColor(colors.text)
      .font('AppRegular')
      .fontSize(13)
      .text(`• ${line}`, card.x + 22, lineY, { width: card.w - 44 });
    lineY += 42;
  }
}

doc
  .fillColor(colors.muted)
  .font('AppRegular')
  .fontSize(12)
  .text(
    'Файл сгенерирован автоматически. Источник логики: Angular App / KmpBridgeService / WebRootBridge / Shared KMP.',
    40,
    900,
    { width: 1200 },
  );

doc.end();

console.log(`Generated PDF: ${outputPath}`);
