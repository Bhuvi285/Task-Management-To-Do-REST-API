import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";

interface PaginationBarProps {
  pageNumber: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  first: boolean;
  last: boolean;
  onPageChange: (page: number) => void;
}

export function PaginationBar({
  pageNumber,
  totalPages,
  totalElements,
  pageSize,
  first,
  last,
  onPageChange,
}: PaginationBarProps) {
  if (totalPages <= 1) return null;

  const start = pageNumber * pageSize + 1;
  const end = Math.min((pageNumber + 1) * pageSize, totalElements);

  const getPages = (): (number | "…")[] => {
    if (totalPages <= 7) return Array.from({ length: totalPages }, (_, i) => i);
    const pages: (number | "…")[] = [0];
    if (pageNumber > 2) pages.push("…");
    for (
      let i = Math.max(1, pageNumber - 1);
      i <= Math.min(totalPages - 2, pageNumber + 1);
      i++
    ) {
      pages.push(i);
    }
    if (pageNumber < totalPages - 3) pages.push("…");
    pages.push(totalPages - 1);
    return pages;
  };

  return (
    <div className="flex flex-col items-center justify-between gap-3 sm:flex-row">
      <p className="text-xs text-muted-foreground">
        Showing {start}–{end} of {totalElements} tasks
      </p>
      <div className="flex items-center gap-1">
        <Button
          variant="outline"
          size="icon"
          className="h-8 w-8"
          disabled={first}
          onClick={() => onPageChange(pageNumber - 1)}
          aria-label="Previous page"
        >
          <ChevronLeft className="h-4 w-4" />
        </Button>
        {getPages().map((page, idx) =>
          page === "…" ? (
            <span key={`ellipsis-${idx}`} className="px-1 text-sm text-muted-foreground">
              …
            </span>
          ) : (
            <Button
              key={page}
              variant={page === pageNumber ? "default" : "outline"}
              size="icon"
              className="h-8 w-8 text-xs"
              onClick={() => onPageChange(page as number)}
              aria-label={`Go to page ${(page as number) + 1}`}
              aria-current={page === pageNumber ? "page" : undefined}
            >
              {(page as number) + 1}
            </Button>
          ),
        )}
        <Button
          variant="outline"
          size="icon"
          className="h-8 w-8"
          disabled={last}
          onClick={() => onPageChange(pageNumber + 1)}
          aria-label="Next page"
        >
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}
