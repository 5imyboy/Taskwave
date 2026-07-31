export const STATUS_ORDER = ["NOT_STARTED", "IN_PROGRESS", "COMPLETED"];

export function getNextStatus(currentStatus: string, forward: boolean): string | null {
  const currentIndex = STATUS_ORDER.indexOf(currentStatus);
  const nextIndex = forward ? currentIndex + 1 : currentIndex - 1;
  if (nextIndex < 0 || nextIndex >= STATUS_ORDER.length) return null;
  return STATUS_ORDER[nextIndex];
}
