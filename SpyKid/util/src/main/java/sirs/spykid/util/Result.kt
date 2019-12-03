package sirs.spykid.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.function.Consumer

@RequiresApi(api = Build.VERSION_CODES.N)
interface Result<T, E> {
    fun isOk(): Boolean
    fun isErr(): Boolean
    fun <U> map(f: (T) -> U): Result<U, E>
    fun <U> mapOrElse(f: (T) -> U, e: (E) -> U): U
    fun <F> mapErr(f: (E) -> F): Result<T, F>
    fun match(ok: Consumer<T>, err: Consumer<E>)
    fun unwrap(): T
    fun unwrapOr(def: T): T
    fun unwrapOrElse(f: (E) -> T): T
    fun unwrapErr(): E
    fun <U> and(res: Result<U, E>): Result<U, E>
    fun <U> andThen(f: (T) -> Result<U, E>): Result<U, E>
    fun <F> or(f: Result<T, F>): Result<T, F>
    fun <F> orElse(f: (E) -> Result<T, F>): Result<T, F>
    fun expect(s: String): T
    fun expectErr(s: String): E

    companion object {
        fun <T> catch(f: () -> T): Result<T, Exception> {
            return try {
                Ok(f())
            } catch (e: Exception) {
                Err(e)
            }
        }
    }

    // TODO: Could not be implemented
    //
    // Option<Result<T, E>> transpose();
    // Result<T, E> clone();
    // T unwrap_or_default();
    // int compareTo(Result<T, E> o);
}

@RequiresApi(api = Build.VERSION_CODES.N)
class Ok<T, E>(val value: T) : Result<T, E> {
    override fun match(ok: Consumer<T>, err: Consumer<E>) {
        ok.accept(value);
    }

    override fun isOk(): Boolean {
        return true
    }

    override fun isErr(): Boolean {
        return false
    }

    override fun <U> map(f: (T) -> U): Result<U, E> {
        val u = f(this.value)
        return Ok(u)
    }

    override fun <U> mapOrElse(f: (T) -> U, e: (E) -> U): U {
        return f(this.value)
    }

    override fun <F> mapErr(f: (E) -> F): Result<T, F> {
        return Ok(this.value)
    }


    override fun unwrap(): T {
        return this.value
    }

    override fun unwrapOr(def: T): T {
        return this.value
    }

    override fun unwrapOrElse(f: (E) -> T): T {
        return this.value
    }

    override fun unwrapErr(): E {
        return this.expectErr("Panicked on unwrapping result.Ok(" + this.value + ")")
    }

    override fun <U> and(res: Result<U, E>): Result<U, E> {
        return res
    }

    override fun <U> andThen(f: (T) -> Result<U, E>): Result<U, E> {
        return f(this.value)
    }

    override fun <F> or(f: Result<T, F>): Result<T, F> {
        return Ok(this.value)
    }

    override fun <F> orElse(f: (E) -> Result<T, F>): Result<T, F> {
        return Ok(this.value)
    }

    override fun expect(s: String): T {
        return this.value
    }

    override fun expectErr(s: String): E {
        throw RuntimeException(s)
    }

    override fun toString(): String {
        return "Ok(" + this.value + ")"
    }
}

@RequiresApi(api = Build.VERSION_CODES.N)
class Err<T, E>(val err: E) : Result<T, E> {
    override fun match(ok: Consumer<T>, err: Consumer<E>) {
        err.accept(this.err)
    }

    override fun isOk(): Boolean {
        return false
    }

    override fun isErr(): Boolean {
        return true
    }

    override fun <U> map(f: (T) -> U): Result<U, E> {
        return Err(this.err)
    }

    override fun <U> mapOrElse(f: (T) -> U, e: (E) -> U): U {
        return e(this.err)
    }

    override fun <F> mapErr(f: (E) -> F): Result<T, F> {
        return Err(f(this.err))
    }

    override fun unwrap(): T {
        return this.expect("Panicked unwrapping result.Err(" + this.err + ")")
    }

    override fun unwrapOr(def: T): T {
        return def
    }

    override fun unwrapOrElse(f: (E) -> T): T {
        return f(this.err)
    }

    override fun unwrapErr(): E {
        return this.err
    }

    override fun <U> and(res: Result<U, E>): Result<U, E> {
        return Err(this.err)
    }

    override fun <U> andThen(f: (T) -> Result<U, E>): Result<U, E> {
        return Err(this.err)
    }

    override fun <F> or(f: Result<T, F>): Result<T, F> {
        return f
    }

    override fun <F> orElse(f: (E) -> Result<T, F>): Result<T, F> {
        return f(this.err)
    }

    override fun expect(s: String): T {
        throw RuntimeException(s)
    }

    override fun expectErr(s: String): E {
        return this.err
    }

    override fun toString(): String {
        return "Err(" + this.err + ")"
    }
}
