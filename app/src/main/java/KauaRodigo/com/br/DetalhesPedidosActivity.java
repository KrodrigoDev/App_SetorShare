package KauaRodigo.com.br;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.w3c.dom.Text;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import KauaRodigo.com.br.model.Pedido;

public class DetalhesPedidosActivity extends AppCompatActivity {


    private CarouselView carouselView;

    private TextView nome;

    private TextView quantidade;

    private TextView codigo;

    private TextView setor;

    private TextView descricao;

    private Pedido pedidoSelecionado;

    private TextView dataAtual;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_pedidos);

        inicializarComponentes();

        // recuperar dados do pedido

        pedidoSelecionado = (Pedido) getIntent().getSerializableExtra("pedidoSelecionado");

        if (pedidoSelecionado != null){

            nome.setText(pedidoSelecionado.getNome());
            quantidade.setText((pedidoSelecionado.getQuantidade()));
            codigo.setText(pedidoSelecionado.getCodigo());
            setor.setText(pedidoSelecionado.getSetor());
            descricao.setText(pedidoSelecionado.getDescricao());
            dataAtual.setText(pedidoSelecionado.getData());


            ImageListener imageListener = new ImageListener() {
                @Override
                public void setImageForPosition(int position, ImageView imageView) {

                    String urlString = pedidoSelecionado.getFotos().get(position);
                             Picasso.get()
                            .load(urlString)
                            .into(imageView);
                }
            };

            carouselView.setPageCount(pedidoSelecionado.getFotos().size());
            carouselView.setImageListener(imageListener);

        }

        ImageView imgVoltarPrincipal = findViewById(R.id.imgVoltarFinal);
        imgVoltarPrincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(DetalhesPedidosActivity.this, PrincipalActivity.class);
                startActivity(in);
            }
        });

    }

    public void visualizarNumeroWhatsApp(View view) {
        String numeroTelefone = "82991305810"; // Substitua pelo número de telefone desejado
        String mensagem = "Olá! Este é um teste do mais novo aplicativo desenvolvido por Kauã Rodrigo de Lima Barbosa"; // Mensagem pré-definida

        // Verifica se o WhatsApp está instalado no dispositivo
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            // Adiciona o número de telefone e a mensagem no formato "https://api.whatsapp.com/send?phone=XXXXXXXXXX&text=Mensagem"
            String url = "https://api.whatsapp.com/send?phone=" + numeroTelefone + "&text=" + URLEncoder.encode(mensagem, "UTF-8");
            intent.setData(Uri.parse(url));
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent);
            } else {
                // WhatsApp não está instalado, trate o caso adequadamente
                Toast.makeText(this, "WhatsApp não está instalado", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void abrirEmailNoGmail(View view) {
        String emailDestino = "kauanrodrigoo25@gmail.com"; // Endereço de e-mail de destino
        String assuntoBase = "Compartilhamento entre setores ";

        // Obter a data do pedido do objeto Pedido
        String dataPedido = pedidoSelecionado.getData();

        // Concatenar o assunto base com a data do pedido
        String assunto = assuntoBase + dataPedido;

        // Extrair informações do pedido
        String setorSolicitante = pedidoSelecionado.getSetor();
        String quantidade = pedidoSelecionado.getQuantidade();
        String codigoMaterial = pedidoSelecionado.getCodigo();

        // Montar o corpo do e-mail
        StringBuilder corpo = new StringBuilder();
        corpo.append("Olá, tudo bem?\n\n");
        corpo.append("Este e-mail é um registro da solicitação realizada na data ").append(dataPedido).append(".\n\n");
        corpo.append("Setor Solicitante: ").append(setorSolicitante).append("\n");
        corpo.append("Quantidade: ").append(quantidade).append("\n");
        corpo.append("Código do Material: ").append(codigoMaterial).append("\n\n");
        corpo.append("Atenciosamente,\nSetorShare\n\n");
        corpo.append("Desenvolvido por Kauã Rodrigo");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailDestino});
        intent.putExtra(Intent.EXTRA_SUBJECT, assunto);
        intent.putExtra(Intent.EXTRA_TEXT, corpo.toString());

        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        } else {
            // O aplicativo do Gmail não está instalado, trate o caso adequadamente
            Toast.makeText(this, "O aplicativo do Gmail não está instalado", Toast.LENGTH_SHORT).show();
        }
    }




    public void inicializarComponentes(){
        carouselView = findViewById(R.id.carouselView);
        nome = findViewById(R.id.textNomeDetalhes);
        quantidade = findViewById(R.id.textQuantidadeDetalhes);
        codigo = findViewById(R.id.textCodigoDetalhes);
        setor = findViewById(R.id.textSetorDetalhes);
        descricao = findViewById(R.id.textDescricaoDetalhes);
        dataAtual = findViewById(R.id.textDataAtualdeta);


    }


}