package KauaRodigo.com.br;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import KauaRodigo.com.br.helper.ConfiguracaoFireBase;
import KauaRodigo.com.br.helper.Permissoes;
import KauaRodigo.com.br.model.Pedido;
import dmax.dialog.SpotsDialog;

public class CadastrarPedidosActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAMERA = 2;

    private EditText campoNome, campoCod, campoQuanti, campoDesc;
    private Button bntFazerPedido;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private ImageView imagem1, imagem2;

    private Spinner campoSetor, campoCategoria;

    private List<String> ListaFotosRecuperadas = new ArrayList<>();

    private List<String> ListaURLFotos = new ArrayList<>();

    private Pedido pedido;

    private StorageReference storage;

    private AlertDialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_pedidos);

        // configurações iniciais
        storage = ConfiguracaoFireBase.getFirebaseStorage();

        //validar permissões
        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();
        carregarDadosSpinner();


        // configurações para notificação

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        String token = task.getResult();
                        System.out.println("TOKEN " + token);

                    }
                });


    }

    public void salvarPedido(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Salvando Pedido...")
                .setCancelable(false)
                .build();
        dialog.show();


        for (int i = 0; i < ListaFotosRecuperadas.size(); i++){

            String urlImagem = ListaFotosRecuperadas.get(i);
            int tamanhoLista = ListaFotosRecuperadas.size();
            salavarFotoStorage(urlImagem, tamanhoLista, i);


        }
    }

    private void salavarFotoStorage(String urlString, int totalFotos, int contador){

        // Criar nó no storage
        final  StorageReference imagemPedido = storage.child("imagensPedidos")
                .child("pedidos")
                .child(pedido.getIdPedido())
                .child("imagem"+ contador);

        // fazer upload do arquivo
        final UploadTask uploadTask = imagemPedido.putFile(Uri.parse(urlString));

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imagemPedido.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        String urlConvertida = uri.toString();

                        ListaURLFotos.add(urlConvertida);

                        if ( ListaURLFotos.size() == totalFotos  ){ //todas as fotos salvas
                            pedido.setFotos( ListaURLFotos );
                            pedido.salvar();

                            dialog.dismiss();
                            finish();
                        }
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                exibirMensagemErro("Falha ao fazer upload");
                Log.e("INFO", "Falha ao fazer upload: " + e.getMessage());
            }
        });
    }

    private Pedido configurarPedido(){

        // Obter a data atual do sistema
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dataAtual = dateFormat.format(new Date());

        String setor = campoSetor.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String nome = campoNome.getText().toString();
        String codigo = campoCod.getText().toString();
        String quantidade = campoQuanti.getText().toString();
        String descricao = campoDesc.getText().toString();


        Pedido pedido = new Pedido();
        pedido.setSetor(setor);
        pedido.setCategoria(categoria);
        pedido.setNome(nome);
        pedido.setCodigo(codigo);
        pedido.setQuantidade(quantidade);
        pedido.setDescricao(descricao);

        pedido.setData(dataAtual);



        return pedido;

    }



    public void validarDadosPedido(View view){

        pedido = configurarPedido();

        if ( ListaFotosRecuperadas.size() != 0 ){

            if(!pedido.getSetor().isEmpty()){
                if(!pedido.getCategoria().isEmpty()){
                    if(!pedido.getNome().isEmpty()){
                        if(!pedido.getCodigo().isEmpty() && !pedido.getCodigo().equals("") && pedido.getCodigo().length() == 7){
                            if(!pedido.getQuantidade().isEmpty()){
                                if(!pedido.getDescricao().isEmpty()){

                                        salvarPedido();


                                } else {

                                    exibirMensagemErro("Preencha o campo descrição !");

                                }
                            } else {

                                exibirMensagemErro("Preencha o campo quantidade !");

                            }
                        } else {

                            exibirMensagemErro("Preencha o campo código, digite os 7 números padrões !");

                        }
                    } else {

                        exibirMensagemErro("Preencha o campo nome !");

                    }
                } else {

                    exibirMensagemErro("Selecione uma categoria !");

                }
            } else {

                exibirMensagemErro("Selecione um setor !");

            }


        } else {

            exibirMensagemErro("Selecione ao menos uma foto !");

        }

    }

    private void exibirMensagemErro(String mensagem){
        Toast.makeText(this, mensagem,
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.imagePedido1) {
            escolherImagem();
        } else if (viewId == R.id.imagePedido2) {
            escolherImagem();
        }
    }

    public void escolherImagem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolher imagem de");
        builder.setItems(new CharSequence[]{"Galeria", "Câmera"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
                    } else {
                        // Solicitar permissão para acessar a galeria de imagens
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_GALLERY);
                    }
                } else {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_IMAGE_CAMERA);
                    } else {
                        // Solicitar permissão para usar a câmera
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAMERA);
                    }
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY) {
                Uri imagemSelecionada = data.getData();
                String caminhoImagem = imagemSelecionada.toString();
                adicionarImagemPedido(caminhoImagem);
            } else if (requestCode == REQUEST_IMAGE_CAMERA) {
                Bitmap imagemCapturada = (Bitmap) data.getExtras().get("data");
                Uri imagemUri = getImageUri(imagemCapturada);
                String caminhoImagem = imagemUri.toString();
                adicionarImagemPedido(caminhoImagem);
            }
        }
    }

    private void adicionarImagemPedido(String caminhoImagem) {
        if (ListaFotosRecuperadas.size() < 2) {
            ListaFotosRecuperadas.add(caminhoImagem);
            ImageView imageView = ListaFotosRecuperadas.size() == 1 ? imagem1 : imagem2;
            imageView.setImageURI(Uri.parse(caminhoImagem));
        } else {
            Toast.makeText(this, "Número máximo de fotos atingido", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    public void inicializarComponentes(){

        campoNome = findViewById(R.id.nomeMaterial);
        campoCod = findViewById(R.id.codMaterial);
        campoQuanti = findViewById(R.id.quantidadeMaterial);
        campoDesc = findViewById(R.id.descMaterial);
        bntFazerPedido = findViewById(R.id.bntCadastrarPedido);
        imagem1 = findViewById(R.id.imagePedido1);
        imagem2 = findViewById(R.id.imagePedido2);
        campoSetor = findViewById(R.id.spinnerSetor);
        campoCategoria = findViewById(R.id.spinnerCategoria);



        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);

    }

    private void carregarDadosSpinner(){

        String[] setor = getResources().getStringArray(R.array.setor);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                setor
        );
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        campoSetor.setAdapter( adapter );

        String[] categorias = getResources().getStringArray(R.array.categoria);
        ArrayAdapter<String> adapterC = new ArrayAdapter<>(
                this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                categorias
        );
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        campoCategoria.setAdapter( adapterC );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_IMAGE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
            } else {
                Toast.makeText(this, "Permissão de acesso à galeria negada", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_IMAGE_CAMERA);
            } else {
                Toast.makeText(this, "Permissão da câmera foi negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permisões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }



}