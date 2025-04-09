package vn.heistom.api;

@FunctionalInterface
public interface ApiCallExecutor<T> {
   ApiCallResult<T> call() throws ApiCallException;
}