package br.com.alura.estoque.repository;

import android.content.Context;

import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.callback.BaseCallBack;
import br.com.alura.estoque.retrofit.callback.NoReturnCallBack;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private final ProdutoService service;

    public ProdutoRepository(Context context) {
        EstoqueDatabase db = EstoqueDatabase.getInstance(context);
        dao = db.getProdutoDAO();
        service = new EstoqueRetrofit().getProdutoService();
    }

    public void buscaProdutos(DataLoadedCallBack<List<Produto>> callBack) {
        buscaProdutosInternos(callBack);
    }

    private void buscaProdutosInternos(DataLoadedCallBack<List<Produto>> callBack) {
        // REGRA DE SYNC, BUSCA INTERNAMENTE, DEPOIS ATUALIZA EXTERNAMENTE
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    //INTERNO
                    callBack.whenSucessful(resultado);
                    //EXTERNO
                    buscaProdutoApi(callBack);
                }).execute();
    }
    private void buscaProdutoApi(DataLoadedCallBack<List<Produto>> callBack) {
        Call<List<Produto>> call = service.buscaTodos();
        call.enqueue(new BaseCallBack<>(new BaseCallBack.ResponseCallBack<List<Produto>>() {
                    @Override
                    public void whenSucessful(List<Produto> resultado) {
                        atualizaInterno(resultado, callBack);
                    }
                    @Override
                    public void whenFailed(String erro) {
                        callBack.whenFailed(erro);
                    }
                }));
    }

    private void atualizaInterno(List<Produto> produtos, DataLoadedCallBack<List<Produto>> callBack) {
        new BaseAsyncTask<>(() -> {
                dao.salva(produtos);
                return dao.buscaTodos();
        }, callBack::whenSucessful)
                .execute();
    }

    public void salva(Produto produto, DataLoadedCallBack<Produto> listenerCallBack) {
        salvaProdutoApi(produto, listenerCallBack);
    }

    private void salvaProdutoApi(Produto produto, DataLoadedCallBack<Produto> listenerCallBack) {
        Call<Produto> call = service.salva(produto);
        call.enqueue(new BaseCallBack<>(new BaseCallBack.ResponseCallBack<Produto>() {
            @Override
            public void whenSucessful(Produto resultado) {
                salvaProdutoInterno(resultado, listenerCallBack);
            }
            @Override
            public void whenFailed(String erro) {
                listenerCallBack.whenFailed(erro);
            }
        }));
    }

    private void salvaProdutoInterno(Produto produto, DataLoadedCallBack<Produto> listenerCallBack) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produto);
            return dao.buscaProduto(id);
        }, listenerCallBack::whenSucessful)
                .execute();
    }

    public void edita(Produto produto, DataLoadedCallBack<Produto> callBack) {
        Call<Produto> call = service.edita(produto.getId(), produto);
        editaProdutoApi(produto, callBack, call);
    }

    private void editaProdutoApi(Produto produto, DataLoadedCallBack<Produto> callBack, Call<Produto> call) {
        call.enqueue(new BaseCallBack<>(new BaseCallBack.ResponseCallBack<Produto>() {
            @Override
            public void whenSucessful(Produto resultado) {
                editaInterno(produto, callBack);
            }
            @Override
            public void whenFailed(String erro) {
                callBack.whenFailed(erro);
            }
        }));
    }

    private void editaInterno(Produto produto, DataLoadedCallBack<Produto> callBack) {
        new BaseAsyncTask<>(() -> {
            dao.atualiza(produto);
            return produto;
        }, callBack::whenSucessful).execute();
    }

    public void remove(Produto produtoRemovido, DataLoadedCallBack<Void> callBack) {
        Call<Void> call = service.remove(produtoRemovido.getId());
        removeProdutoApi(produtoRemovido, callBack, call);
    }

    private void removeProdutoApi(Produto produtoRemovido, DataLoadedCallBack<Void> callBack, Call<Void> call) {
        call.enqueue(new NoReturnCallBack(new NoReturnCallBack.ResponseCallBack() {
            @Override
            public void whenSucessful() {
                removeInterno(produtoRemovido, callBack);
            }
            @Override
            public void whenFailed(String erro) {
                callBack.whenFailed(erro);
            }
        }));
    }

    private void removeInterno(Produto produtoRemovido, DataLoadedCallBack<Void> callBack) {
        new BaseAsyncTask<>(() -> {
            dao.remove(produtoRemovido);
            return null;
        },callBack::whenSucessful)
            .execute();
    }

    public interface DataLoadedCallBack<T>{
        void whenSucessful(T resultado);
        void whenFailed(String error);
    }
}
