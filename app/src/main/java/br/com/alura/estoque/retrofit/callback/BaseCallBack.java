package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static br.com.alura.estoque.retrofit.callback.MessagesCallback.FALHA_NA_COMUNICACAO;
import static br.com.alura.estoque.retrofit.callback.MessagesCallback.RESPOSTA_NAO_SUCEDIDA;

public class BaseCallBack<T> implements Callback<T> {

    private final ResponseCallBack<T> callBack;

    public BaseCallBack(ResponseCallBack<T> callBack) {
        this.callBack = callBack;
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<T> call, Response<T> response) {
        if(response.isSuccessful()){
            T result = response.body();
            if(result != null) callBack.whenSucessful(result);
        } else callBack.whenFailed(RESPOSTA_NAO_SUCEDIDA);
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<T> call, Throwable t) {
        callBack.whenFailed(FALHA_NA_COMUNICACAO + t.getMessage());
    }

    public interface ResponseCallBack<T>{
        void whenSucessful(T resultado);
        void whenFailed(String erro);
    }
}