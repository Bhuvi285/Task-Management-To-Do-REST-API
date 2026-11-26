import { CheckSquare } from "lucide-react";
import { cn } from "@/lib/utils";

export function BrandLogo({ className }: { className?: string }) {
  return (
    <div className={cn("flex items-center gap-2.5", className)}>
      <div className="tf-gradient-bg flex h-8 w-8 items-center justify-center rounded-lg text-white shadow-md shadow-indigo-500/25">
        <CheckSquare className="h-4 w-4" />
      </div>
      <div className="flex items-center gap-1.5">
        <span className="font-display text-lg font-bold tracking-tight text-on-surface">
          TaskFlow
        </span>
        <span className="rounded-full bg-secondary-container px-1.5 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-on-secondary-container">
          PRO
        </span>
      </div>
    </div>
  );
}
