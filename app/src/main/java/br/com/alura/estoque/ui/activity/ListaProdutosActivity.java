package br.com.alura.estoque.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import br.com.alura.estoque.R;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.repository.ProdutoRepository;
import br.com.alura.estoque.ui.dialog.EditaProdutoDialog;
import br.com.alura.estoque.ui.dialog.SalvaProdutoDialog;
import br.com.alura.estoque.ui.recyclerview.adapter.ListaProdutosAdapter;

public class ListaProdutosActivity extends AppCompatActivity {

    private static final String MENSAGEM_ERRO_BUSCA_PRODUTO = "Não foi possível carregar novos produtos";
    private static final String MENSAGEM_ERRO_REMOVE_PRODUTO = "Não foi possível remover o produto";
    private static final String MENSAGEM_ERRO_SALVA_PRODUTO  = "Não foi possível salvar o produto";
    private static final String MENSAGEM_ERRO_EDITA_PRODUTO  = "Não foi possível editar o produto";
    private static final String TITULO_APPBAR = "Lista de produtos";
    private ListaProdutosAdapter adapter;
    private ProdutoRepository produtoRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);
        setTitle(TITULO_APPBAR);

        configuraListaProdutos();
        configuraFabSalvaProduto();

        produtoRepository = new ProdutoRepository(this);
        buscaProdutos();
    }

    private void buscaProdutos() {
        produtoRepository.buscaProdutos(new ProdutoRepository.DataLoadedCallBack<List<Produto>>() {
            @Override
            public void whenSucessful(List<Produto> resultado) {
                adapter.atualiza(resultado);
            }
            @Override
            public void whenFailed(String error) {
                mostraErro(MENSAGEM_ERRO_BUSCA_PRODUTO, Toast.LENGTH_LONG);
            }
        });
    }

    private void mostraErro(String mensagem, int lengthLong) {
        Toast.makeText(ListaProdutosActivity.this, mensagem, lengthLong).show();
    }
    private void configuraListaProdutos() {
        RecyclerView listaProdutos = findViewById(R.id.activity_lista_produtos_lista);
        adapter = new ListaProdutosAdapter(this, this::abreFormularioEditaProduto);
        listaProdutos.setAdapter(adapter);
        adapter.setOnItemClickRemoveContextMenuListener((
                (posicao, produtoRemovido) -> produtoRepository.remove(produtoRemovido,
                new ProdutoRepository.DataLoadedCallBack<Void>() {
                    @Override
                    public void whenSucessful(Void resultado) {
                        adapter.remove(posicao);
                    }
                    @Override
                    public void whenFailed(String error) {
                        mostraErro(MENSAGEM_ERRO_REMOVE_PRODUTO, Toast.LENGTH_SHORT);
                    }
         })));
    }

    private void configuraFabSalvaProduto() {
        FloatingActionButton fabAdicionaProduto = findViewById(R.id.activity_lista_produtos_fab_adiciona_produto);
        fabAdicionaProduto.setOnClickListener(v -> abreFormularioSalvaProduto());
    }

    private void abreFormularioSalvaProduto() {
        new SalvaProdutoDialog(this, produtoCriado ->
            produtoRepository.salva(produtoCriado, new ProdutoRepository.DataLoadedCallBack<Produto>() {
                @Override
                public void whenSucessful(Produto resultado) {
                    adapter.adiciona(resultado);
                }
                @Override
                public void whenFailed(String error) {
                    mostraErro(MENSAGEM_ERRO_SALVA_PRODUTO ,  Toast.LENGTH_LONG);
                }
            })).mostra();
    }

    private void abreFormularioEditaProduto(int posicao, Produto produto) {
        new EditaProdutoDialog(this, produto,
                produtoCriado -> produtoRepository.edita(produtoCriado, new ProdutoRepository.DataLoadedCallBack<Produto>() {
                    @Override
                    public void whenSucessful(Produto produtoEditado) {
                        adapter.edita(posicao,produtoEditado);
                    }
                    @Override
                    public void whenFailed(String error) {
                        mostraErro(MENSAGEM_ERRO_EDITA_PRODUTO ,  Toast.LENGTH_SHORT);
                    }
                }))
                .mostra();
    }
}