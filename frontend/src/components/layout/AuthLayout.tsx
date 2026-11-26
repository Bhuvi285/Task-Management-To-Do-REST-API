import { Outlet } from "react-router-dom";
import { BrandLogo } from "@/components/layout/BrandLogo";
import { ThemeToggle } from "@/components/layout/ThemeToggle";

export function AuthLayout() {
  return (
    <div className="tf-mesh-bg relative flex min-h-screen flex-col justify-between bg-background p-4 sm:p-6">
      <header className="mx-auto flex w-full max-w-5xl items-center justify-between">
        <BrandLogo />
        <ThemeToggle />
      </header>

      <main className="mx-auto my-8 w-full max-w-md">
        <div className="glass-surface rounded-2xl p-6 sm:p-8">
          <Outlet />
        </div>
      </main>

      <footer className="mx-auto w-full max-w-5xl text-center text-xs text-muted-foreground">
        TaskFlow &bull; Secure Task Management
      </footer>
    </div>
  );
}
