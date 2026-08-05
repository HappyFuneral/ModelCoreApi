package org.justice.modelapicorereloaded.response;

public class Response<T> {
    private boolean success;
    private String message;
    private T data;

    private Response(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> ok(T data) {
        return new Response<>(true, "Success", data);
    }

    public static <T> Response<T> ok(String message, T data) {
        return new Response<>(true, message, data);
    }

    public static <T> Response<T> error(String message) {
        return new Response<>(false, message, null);
    }

    public static <T> Response<T> error(String message, T data) {
        return new Response<>(false, message, data);
    }

    // Getters (és ha kell, Setters is)
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}