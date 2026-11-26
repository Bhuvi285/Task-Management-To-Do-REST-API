import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Eye, EyeOff, KeyRound, Loader2, Moon, Sun, SunMoon, User2 } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useToast } from "@/components/ui/toast";
import { useTheme } from "@/components/theme/ThemeProvider";
import { useAuth } from "@/features/auth/AuthContext";
import { authService } from "@/features/auth/api/authService";
import { normalizeApiError } from "@/lib/api/errors";
import { changePasswordSchema, type ChangePasswordFormValues } from "@/features/auth/schemas/authSchemas";

// ─── Theme Picker ────────────────────────────────────────────────────────────

function ThemeSection() {
  const { theme, setTheme } = useTheme();

  const options = [
    { value: "light" as const, label: "Light", icon: <Sun className="h-4 w-4" /> },
    { value: "dark" as const, label: "Dark", icon: <Moon className="h-4 w-4" /> },
    { value: "system" as const, label: "System", icon: <SunMoon className="h-4 w-4" /> },
  ];

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-2">
          <SunMoon className="h-4 w-4 text-primary" />
          <CardTitle className="text-base">Appearance</CardTitle>
        </div>
        <CardDescription>Choose your preferred color theme.</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="flex gap-3">
          {options.map((opt) => (
            <button
              key={opt.value}
              onClick={() => setTheme(opt.value)}
              className={`flex flex-1 flex-col items-center gap-2 rounded-xl border-2 p-4 text-sm font-medium transition-all
                ${
                  theme === opt.value
                    ? "border-primary bg-primary/10 text-primary"
                    : "border-border bg-muted/30 text-muted-foreground hover:border-primary/40 hover:bg-muted/60"
                }
              `}
            >
              {opt.icon}
              {opt.label}
            </button>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}

// ─── Profile Section ─────────────────────────────────────────────────────────

function ProfileSection({ email }: { email: string }) {
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-2">
          <User2 className="h-4 w-4 text-primary" />
          <CardTitle className="text-base">Profile</CardTitle>
        </div>
        <CardDescription>Your account identity from the backend.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-1.5">
          <Label>Email</Label>
          <div className="flex h-10 items-center rounded-lg border border-border/60 bg-muted/40 px-3 text-sm text-foreground">
            {email}
          </div>
          <p className="text-xs text-muted-foreground">
            Your email is linked to your account and cannot be changed here.
          </p>
        </div>
        <div className="rounded-lg border border-border/40 bg-muted/20 p-3 text-xs text-muted-foreground">
          <strong className="text-foreground">Note:</strong> Name and profile picture editing requires a backend{" "}
          <code>/users/me</code> endpoint that is not yet implemented. Only email (from your JWT) is available.
        </div>
      </CardContent>
    </Card>
  );
}

// ─── Change Password Section ──────────────────────────────────────────────────

function ChangePasswordSection() {
  const { toast } = useToast();
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    setError,
  } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema) as never,
  });

  const onSubmit = async (values: ChangePasswordFormValues) => {
    setIsSubmitting(true);
    try {
      await authService.changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      toast({
        title: "Password changed",
        description: "All sessions revoked. Please log in with your new password.",
        variant: "success",
      });
      reset();
      // Backend revokes all sessions — log out and redirect
      await logout();
      navigate("/auth/login", { replace: true });
    } catch (err) {
      const apiErr = normalizeApiError(err);
      if (apiErr.status === 400 || apiErr.status === 401) {
        setError("currentPassword", { message: apiErr.message });
      } else {
        toast({
          title: "Failed to change password",
          description: apiErr.message,
          variant: "error",
        });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-2">
          <KeyRound className="h-4 w-4 text-primary" />
          <CardTitle className="text-base">Change Password</CardTitle>
        </div>
        <CardDescription>
          After changing your password, all existing sessions (including this one) will be revoked and
          you will be redirected to log in again.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit(onSubmit as never)} className="space-y-4 max-w-md">
          {/* Current password */}
          <div className="space-y-1.5">
            <Label htmlFor="cp-current">Current Password</Label>
            <div className="relative">
              <Input
                id="cp-current"
                type={showCurrent ? "text" : "password"}
                placeholder="Enter current password"
                {...register("currentPassword")}
                aria-invalid={Boolean(errors.currentPassword)}
                className="pr-10"
              />
              <button
                type="button"
                tabIndex={-1}
                onClick={() => setShowCurrent((v) => !v)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                aria-label={showCurrent ? "Hide password" : "Show password"}
              >
                {showCurrent ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
            {errors.currentPassword && (
              <p className="text-xs text-rose-500">{errors.currentPassword.message}</p>
            )}
          </div>

          {/* New password */}
          <div className="space-y-1.5">
            <Label htmlFor="cp-new">New Password</Label>
            <div className="relative">
              <Input
                id="cp-new"
                type={showNew ? "text" : "password"}
                placeholder="Enter new password (8-72 chars)"
                {...register("newPassword")}
                aria-invalid={Boolean(errors.newPassword)}
                className="pr-10"
              />
              <button
                type="button"
                tabIndex={-1}
                onClick={() => setShowNew((v) => !v)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                aria-label={showNew ? "Hide password" : "Show password"}
              >
                {showNew ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
            {errors.newPassword && (
              <p className="text-xs text-rose-500">{errors.newPassword.message}</p>
            )}
          </div>

          {/* Confirm password */}
          <div className="space-y-1.5">
            <Label htmlFor="cp-confirm">Confirm New Password</Label>
            <div className="relative">
              <Input
                id="cp-confirm"
                type={showConfirm ? "text" : "password"}
                placeholder="Re-enter new password"
                {...register("confirmPassword")}
                aria-invalid={Boolean(errors.confirmPassword)}
                className="pr-10"
              />
              <button
                type="button"
                tabIndex={-1}
                onClick={() => setShowConfirm((v) => !v)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                aria-label={showConfirm ? "Hide password" : "Show password"}
              >
                {showConfirm ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
            {errors.confirmPassword && (
              <p className="text-xs text-rose-500">{errors.confirmPassword.message}</p>
            )}
          </div>

          <div className="pt-1">
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting && <Loader2 className="h-4 w-4 animate-spin" />}
              Change Password
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}

// ─── Main Page ───────────────────────────────────────────────────────────────

export function SettingsPage() {
  const { user } = useAuth();

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div>
        <h1 className="tf-gradient-text text-2xl font-bold font-display">Settings</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Manage your account preferences
        </p>
      </div>

      <ThemeSection />
      <ProfileSection email={user?.email ?? "—"} />
      <ChangePasswordSection />
    </div>
  );
}
