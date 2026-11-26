import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";

export function NotFoundPage() {
  return (
    <div className="tf-mesh-bg flex min-h-screen flex-col items-center justify-center p-6 text-center">
      <div className="glass-surface max-w-md rounded-2xl p-8">
        <p className="tf-gradient-text font-display text-5xl font-extrabold">404</p>
        <h1 className="mt-3 font-display text-xl font-semibold">Page not found</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          The page you are looking for does not exist or has been moved.
        </p>
        <Link to="/dashboard" className="mt-6 inline-block">
          <Button>Return to Dashboard</Button>
        </Link>
      </div>
    </div>
  );
}
