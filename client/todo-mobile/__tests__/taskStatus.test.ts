import { getNextStatus } from "../lib/taskStatus";

describe("getNextStatus", () => {
  describe("moving forward", () => {
    test("NOT_STARTED → IN_PROGRESS", () => {
      expect(getNextStatus("NOT_STARTED", true)).toBe("IN_PROGRESS");
    });

    test("IN_PROGRESS → COMPLETED", () => {
      expect(getNextStatus("IN_PROGRESS", true)).toBe("COMPLETED");
    });

    test("cannot advance past COMPLETED", () => {
      expect(getNextStatus("COMPLETED", true)).toBeNull();
    });
  });

  describe("moving backward", () => {
    test("COMPLETED → IN_PROGRESS", () => {
      expect(getNextStatus("COMPLETED", false)).toBe("IN_PROGRESS");
    });

    test("IN_PROGRESS → NOT_STARTED", () => {
      expect(getNextStatus("IN_PROGRESS", false)).toBe("NOT_STARTED");
    });

    test("cannot go back past NOT_STARTED", () => {
      expect(getNextStatus("NOT_STARTED", false)).toBeNull();
    });
  });
});
