package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static br.com.alura.estoque.retrofit.callback.MessagesCallback.FALHA_NA_COMUNICACAO;
import static br.com.alura.estoque.retrofit.callback.MessagesCallback.RESPOSTA_NAO_SUCEDIDA;


public class NoReturnCallBack implements Callback<Void> {


    private final ResponseCallBack callBack;

    public NoReturnCallBack(ResponseCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<Void> call, Response<Void> response) {
        if(response.isSuccessful()){
            callBack.whenSucessful();
        } else{
            callBack.whenFailed(RESPOSTA_NAO_SUCEDIDA);
        }
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<Void> call, Throwable t) {
        callBack.whenFailed(FALHA_NA_COMUNICACAO + t.getMessage());
    }

    public interface ResponseCallBack<T>{
        void whenSucessful();
        void whenFailed(String erro);
    }
}
