import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useSearchParams } from "react-router-dom";
import { AlertCircle, ArrowLeft, CheckCircle2, Eye, EyeOff, KeyRound, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { authService } from "@/features/auth/api/authService";
import {
  resetPasswordSchema,
  type ResetPasswordFormValues,
} from "@/features/auth/schemas/authSchemas";
import { normalizeApiError } from "@/lib/api/errors";

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const initialToken = searchParams.get("token") ?? "";
  const [showPassword, setShowPassword] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      resetToken: initialToken,
      newPassword: "",
      confirmPassword: "",
    },
  });

  const onSubmit = async (values: ResetPasswordFormValues) => {
    setServerError(null);
    try {
      const response = await authService.resetPassword({
        resetToken: values.resetToken.trim(),
        newPassword: values.newPassword,
      });
      setSuccessMessage(response.message);
    } catch (err) {
      setServerError(normalizeApiError(err).message);
    }
  };

  if (successMessage) {
    return (
      <div className="space-y-5 text-center">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-500/15 text-emerald-500">
          <CheckCircle2 className="h-6 w-6" />
        </div>
        <div className="space-y-1.5">
          <h1 className="font-display text-2xl font-bold">Password Updated</h1>
          <p className="text-sm text-muted-foreground">{successMessage}</p>
        </div>
        <Link to="/auth/login" className="block">
          <Button className="w-full">Sign in with new password</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="space-y-1.5">
        <h1 className="font-display text-2xl font-bold tracking-tight">Choose a new password</h1>
        <p className="text-sm text-muted-foreground">
          Enter your single-use reset token and your new password.
        </p>
      </div>

      {serverError ? (
        <div
          role="alert"
          className="flex items-start gap-2.5 rounded-xl border border-rose-500/30 bg-rose-500/10 p-3 text-sm text-rose-600 dark:text-rose-400"
        >
          <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
          <span>{serverError}</span>
        </div>
      ) : null}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="space-y-1.5">
          <Label htmlFor="reset-token">Reset token</Label>
          <Input
            id="reset-token"
            type="text"
            placeholder="Paste your password-reset token"
            aria-invalid={Boolean(errors.resetToken)}
            {...register("resetToken")}
          />
          {errors.resetToken ? (
            <p className="text-xs text-rose-500">{errors.resetToken.message}</p>
          ) : null}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="reset-new-password">New password</Label>
          <div className="relative">
            <Input
              id="reset-new-password"
              type={showPassword ? "text" : "password"}
              autoComplete="new-password"
              placeholder="At least 8 characters"
              className="pr-10"
              aria-invalid={Boolean(errors.newPassword)}
              {...register("newPassword")}
            />
            <button
              type="button"
              onClick={() => setShowPassword((prev) => !prev)}
              className="absolute inset-y-0 right-0 flex items-center px-3 text-muted-foreground hover:text-foreground cursor-pointer"
              aria-label={showPassword ? "Hide password" : "Show password"}
            >
              {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          </div>
          {errors.newPassword ? (
            <p className="text-xs text-rose-500">{errors.newPassword.message}</p>
          ) : null}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="reset-confirm-password">Confirm new password</Label>
          <Input
            id="reset-confirm-password"
            type={showPassword ? "text" : "password"}
            autoComplete="new-password"
            placeholder="Re-enter your new password"
            aria-invalid={Boolean(errors.confirmPassword)}
            {...register("confirmPassword")}
          />
          {errors.confirmPassword ? (
            <p className="text-xs text-rose-500">{errors.confirmPassword.message}</p>
          ) : null}
        </div>

        <Button type="submit" className="w-full" disabled={isSubmitting}>
          {isSubmitting ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Resetting password...
            </>
          ) : (
            <>
              <KeyRound className="h-4 w-4" />
              Reset password
            </>
          )}
        </Button>
      </form>

      <Link
        to="/auth/login"
        className="inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-3.5 w-3.5" />
        Back to sign in
      </Link>
    </div>
  );
}
