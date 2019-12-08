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
    fun match(ok: Consumer<T>, err: Consumer<E>)

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
data class Ok<T, E>(val value: T) : Result<T, E> {
    override fun isOk(): Boolean = true

    override fun isErr(): Boolean = false

    override fun <U> map(f: (T) -> U): Result<U, E> = Ok(f(this.value))

    override fun <U> mapOrElse(f: (T) -> U, e: (E) -> U): U = f(this.value)

    override fun <F> mapErr(f: (E) -> F): Result<T, F> = Ok(this.value)

    override fun unwrap(): T = this.value

    override fun unwrapOr(def: T): T = this.value

    override fun unwrapOrElse(f: (E) -> T): T = this.value

    override fun unwrapErr(): E =
        this.expectErr("Panicked on unwrapping result.Ok(" + this.value + ")")

    override fun <U> and(res: Result<U, E>): Result<U, E> = res

    override fun <U> andThen(f: (T) -> Result<U, E>): Result<U, E> = f(this.value)

    override fun <F> or(f: Result<T, F>): Result<T, F> = Ok(this.value)

    override fun <F> orElse(f: (E) -> Result<T, F>): Result<T, F> = Ok(this.value)

    override fun match(ok: Consumer<T>, err: Consumer<E>): Unit = ok.accept(value)

    override fun expect(s: String): T = this.value

    override fun expectErr(s: String): E = throw RuntimeException(s)

    override fun toString(): String = "Ok(" + this.value + ")"
}

@RequiresApi(api = Build.VERSION_CODES.N)
data class Err<T, E>(val err: E) : Result<T, E> {
    override fun match(ok: Consumer<T>, err: Consumer<E>) = err.accept(this.err)

    override fun isOk(): Boolean = false

    override fun isErr(): Boolean = true

    override fun <U> map(f: (T) -> U): Result<U, E> = Err(this.err)

    override fun <U> mapOrElse(f: (T) -> U, e: (E) -> U): U = e(this.err)

    override fun <F> mapErr(f: (E) -> F): Result<T, F> = Err(f(this.err))

    override fun unwrap(): T = this.expect("Panicked unwrapping result.Err(" + this.err + ")")

    override fun unwrapOr(def: T): T = def

    override fun unwrapOrElse(f: (E) -> T): T = f(this.err)

    override fun unwrapErr(): E = this.err

    override fun <U> and(res: Result<U, E>): Result<U, E> = Err(this.err)

    override fun <U> andThen(f: (T) -> Result<U, E>): Result<U, E> = Err(this.err)

    override fun <F> or(f: Result<T, F>): Result<T, F> = f

    override fun <F> orElse(f: (E) -> Result<T, F>): Result<T, F> = f(this.err)

    override fun expect(s: String): T = throw RuntimeException(s)

    override fun expectErr(s: String): E = this.err

    override fun toString(): String = "Err(" + this.err + ")"
}
