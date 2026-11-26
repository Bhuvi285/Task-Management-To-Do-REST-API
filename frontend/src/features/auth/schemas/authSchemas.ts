import { z } from "zod";

const utf8ByteLength = (value: string) => new TextEncoder().encode(value).length;

const newPasswordSchema = z
  .string()
  .min(8, "Password must be at least 8 characters.")
  .max(72, "Password must not exceed 72 characters.")
  .refine((val) => utf8ByteLength(val) <= 72, {
    message: "Password must not exceed 72 UTF-8 bytes.",
  });

const currentOrLoginPasswordSchema = z
  .string()
  .min(1, "Password is required.")
  .max(72, "Password must not exceed 72 characters.")
  .refine((val) => utf8ByteLength(val) <= 72, {
    message: "Password must not exceed 72 UTF-8 bytes.",
  });

const emailSchema = z
  .string()
  .trim()
  .min(1, "Email is required.")
  .email("Please enter a valid email address.")
  .max(254, "Email must not exceed 254 characters.");

export const loginSchema = z.object({
  email: emailSchema,
  password: currentOrLoginPasswordSchema,
});

export type LoginFormValues = z.infer<typeof loginSchema>;

export const registerSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(1, "Name is required.")
      .max(100, "Name must not exceed 100 characters."),
    email: emailSchema,
    password: newPasswordSchema,
    confirmPassword: z.string().min(1, "Please confirm your password."),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords do not match.",
    path: ["confirmPassword"],
  });

export type RegisterFormValues = z.infer<typeof registerSchema>;

export const forgotPasswordSchema = z.object({
  email: emailSchema,
});

export type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>;

export const resetPasswordSchema = z
  .object({
    resetToken: z
      .string()
      .trim()
      .min(1, "Reset token is required.")
      .max(128, "Reset token must not exceed 128 characters."),
    newPassword: newPasswordSchema,
    confirmPassword: z.string().min(1, "Please confirm your new password."),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords do not match.",
    path: ["confirmPassword"],
  });

export type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;

export const changePasswordSchema = z
  .object({
    currentPassword: currentOrLoginPasswordSchema,
    newPassword: newPasswordSchema,
    confirmPassword: z.string().min(1, "Please confirm your new password."),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords do not match.",
    path: ["confirmPassword"],
  })
  .refine((data) => data.currentPassword !== data.newPassword, {
    message: "New password must differ from current password.",
    path: ["newPassword"],
  });

export type ChangePasswordFormValues = z.infer<typeof changePasswordSchema>;
