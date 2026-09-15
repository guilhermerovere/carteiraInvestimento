export function cnpjDigits(value: string): string {
  return value.replace(/[^0-9]/g, "").slice(0, 14);
}

export function formatCnpj(value: string): string {
  const digits = cnpjDigits(value);
  if (digits.length <= 2) return digits;
  if (digits.length <= 5) return `${digits.slice(0, 2)}.${digits.slice(2)}`;
  if (digits.length <= 8) return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5)}`;
  if (digits.length <= 12) return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}/${digits.slice(8)}`;
  return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}/${digits.slice(8, 12)}-${digits.slice(12)}`;
}

export function cnpjCaretPosition(formattedValue: string, digitCount: number): number {
  if (digitCount <= 0) return 0;
  let seen = 0;
  for (let index = 0; index < formattedValue.length; index += 1) {
    if (/[0-9]/.test(formattedValue[index])) seen += 1;
    if (seen === digitCount) return index + 1;
  }
  return formattedValue.length;
}

export function isValidCnpj(value: string): boolean {
  const digits = cnpjDigits(value);
  if (digits.length !== 14 || /^([0-9])\1{13}$/.test(digits)) return false;
  return checkDigit(digits.slice(0, 12), [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]) === Number(digits[12])
    && checkDigit(digits.slice(0, 13), [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]) === Number(digits[13]);
}

function checkDigit(value: string, weights: number[]): number {
  const remainder = value.split("").reduce((sum, digit, index) => sum + Number(digit) * weights[index], 0) % 11;
  return remainder < 2 ? 0 : 11 - remainder;
}
