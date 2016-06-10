package org.hive2hive.core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sun.istack.internal.Nullable;

/**
 * The Interface CompletableExecutorService.
 */
public interface CompletableExecutorService extends ExecutorService {
	
	/**
	 * Submit.
	 *
	 * @param <T> the generic type
	 * @param task the task
	 * @return a completable future representing pending completion of the task,
	 *         never missing
	 */
	@Override
	<T> CompletableFuture<T> submit(final Callable<T> task);

	/**
	 * Submit.
	 *
	 * @param <T> the generic type
	 * @param task the task
	 * @param result the result
	 * @return a completable future representing pending completion of the task,
	 *         never missing
	 */

	@Override
	<T> CompletableFuture<T> submit(final Runnable task, @Nullable final T result);

	/**
	 * Submit.
	 *
	 * @param task the task
	 * @return a completable future representing pending completion of the task,
	 *         never missing
	 */

	@Override
	CompletableFuture<?> submit(final Runnable task);

	/**
	 * Completable.
	 *
	 * @param executor the executor
	 * @return the completable executor service
	 */
	public static CompletableExecutorService completable(final ExecutorService executor) {
		return newMixin(CompletableExecutorService.class, new Overrides(executor), executor);
	}

	/**
	 * New mixin.
	 *
	 * @param completableExecutorService the completable executor service
	 * @param overrides the overrides
	 * @param executor the executor
	 * @return the completable executor service
	 */
	static CompletableExecutorService newMixin(Class<CompletableExecutorService> completableExecutorService,
			Overrides overrides, ExecutorService executor) {
		return new CompletableExecutorService() {

			@Override
			public void execute(Runnable command) {
				executor.execute(command);
			}

			@Override
			public List<Runnable> shutdownNow() {
				return executor.shutdownNow();
			}

			@Override
			public void shutdown() {
				executor.shutdownNow();
			}

			@Override
			public boolean isTerminated() {
				return executor.isTerminated();
			}

			@Override
			public boolean isShutdown() {
				return executor.isShutdown();
			}

			@Override
			public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				return executor.invokeAny(tasks, timeout, unit);
			}

			@Override
			public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
					throws InterruptedException, ExecutionException {
				return executor.invokeAny(tasks);
			}

			@Override
			public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
					throws InterruptedException {
				return executor.invokeAll(tasks, timeout, unit);
			}

			@Override
			public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
				return executor.invokeAll(tasks);
			}

			@Override
			public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
				return executor.awaitTermination(timeout, unit);
			}

			@Override
			public CompletableFuture<?> submit(Runnable task) {
				return overrides.submit(callable(task, null));
			}

			@Override
			public <T> CompletableFuture<T> submit(Runnable task, T result) {
				return overrides.submit(task, result);
			}

			@Override
			public <T> CompletableFuture<T> submit(Callable<T> task) {
				return overrides.submit(task);
			}
		};
	}

	/**
	 * Callable.
	 *
	 * @param <T> the generic type
	 * @param task the task
	 * @param result the result
	 * @return the callable
	 */
	static <T> Callable<T> callable(Runnable task, T result) {
		return new Callable<T>() {

			@Override
			public T call() throws Exception {
				task.run();
				return result;
			}
		};
	}

	/**
	 * The Class Overrides.
	 */
	public static final class Overrides {
		
		/** The executor. */
		private final ExecutorService executor;

		/**
		 * Instantiates a new overrides.
		 *
		 * @param executor the executor
		 */
		private Overrides(final ExecutorService executor) {
			this.executor = executor;
		}

		/**
		 * Submit.
		 *
		 * @param <T> the generic type
		 * @param task the task
		 * @return the completable future
		 */
		public <T> CompletableFuture<T> submit(final Callable<T> task) {
			final CompletableFuture<T> cf = new UnwrappedCompletableFuture<>();
			executor.submit(() -> {
				try {
					cf.complete(task.call());
				} catch (final CancellationException e) {
					cf.cancel(true);
				} catch (final Exception e) {
					cf.completeExceptionally(e);
				}
			});
			return cf;
		}

		/**
		 * Submit.
		 *
		 * @param <T> the generic type
		 * @param task the task
		 * @param result the result
		 * @return the completable future
		 */
		public <T> CompletableFuture<T> submit(final Runnable task, @Nullable final T result) {
			return submit(callable(task, result));
		}

		/**
		 * The Class UnwrappedCompletableFuture.
		 *
		 * @param <T> the generic type
		 */
		private static final class UnwrappedCompletableFuture<T> extends CompletableFuture<T> {
			
			/* (non-Javadoc)
			 * @see java.util.concurrent.CompletableFuture#get()
			 */
			@Override
			public T get() throws InterruptedException, ExecutionException {
				return UnwrappedInterrupts.<T, RuntimeException> unwrap(super::get);
			}

			/* (non-Javadoc)
			 * @see java.util.concurrent.CompletableFuture#get(long, java.util.concurrent.TimeUnit)
			 */
			@Override
			public T get(final long timeout, final TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				return UnwrappedInterrupts.<T, TimeoutException> unwrap(() -> super.get(timeout, unit));
			}

			/**
			 * The Interface UnwrappedInterrupts.
			 *
			 * @param <T> the generic type
			 * @param <E> the element type
			 */
			@FunctionalInterface
			private interface UnwrappedInterrupts<T, E extends Exception> {
				
				/**
				 * Gets the.
				 *
				 * @return the t
				 * @throws InterruptedException the interrupted exception
				 * @throws ExecutionException the execution exception
				 * @throws E the e
				 */
				T get() throws InterruptedException, ExecutionException, E;

				/**
				 * Unwrap.
				 *
				 * @param <T> the generic type
				 * @param <E> the element type
				 * @param wrapped the wrapped
				 * @return the t
				 * @throws InterruptedException the interrupted exception
				 * @throws ExecutionException the execution exception
				 * @throws E the e
				 */
				static <T, E extends Exception> T unwrap(final UnwrappedInterrupts<T, E> wrapped)
						throws InterruptedException, ExecutionException, E {
					try {
						return wrapped.get();
					} catch (final ExecutionException e) {
						final Throwable cause = e.getCause();
						if (cause instanceof InterruptedException)
							throw (InterruptedException) cause;
						throw e;
					}
				}
			}
		}
	}
}