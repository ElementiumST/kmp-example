const ERROR_MESSAGES: Readonly<Record<string, string>> = {
  EMPTY: 'Поле обязательно',
  TOO_LONG: 'Превышена максимальная длина',
  INVALID_FORMAT: 'Неверный формат',
};

export const mapEditorErrorCode = (code: string | null | undefined): string => {
  if (!code) {
    return '';
  }

  return ERROR_MESSAGES[code] ?? code;
};
