"use client";

import { useEffect, useRef } from "react";

export function useDismissibleDetails() {
  const ref = useRef<HTMLDetailsElement>(null);
  useEffect(() => {
    const dismissOutside = (event: PointerEvent) => {
      const node = ref.current;
      if (node?.open && event.target instanceof Node && !node.contains(event.target)) node.removeAttribute("open");
    };
    const dismissByKeyboard = (event: KeyboardEvent) => {
      const node = ref.current;
      if (event.key !== "Escape" || !node?.open) return;
      node.removeAttribute("open");
      node.querySelector<HTMLElement>("summary")?.focus();
    };
    document.addEventListener("pointerdown", dismissOutside);
    document.addEventListener("keydown", dismissByKeyboard);
    return () => { document.removeEventListener("pointerdown", dismissOutside); document.removeEventListener("keydown", dismissByKeyboard); };
  }, []);
  return ref;
}
