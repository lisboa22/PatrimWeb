package br.com.patrimweb.model;

public class PerfilPermissao {

    private Perfil perfil;
    private Permissao permissao;
    private boolean podeVisualizar;
    private boolean podeInserir;
    private boolean podeEditar;
    private boolean podeExcluir;
    
    
    public PerfilPermissao() {
    }
    
    
    
    public PerfilPermissao(Perfil perfil, Permissao permissao, boolean podeVisualizar, 
    		boolean podeInserir, boolean podeEditar, boolean podeExcluir) {
        this.perfil = perfil;
        this.permissao = permissao;
        this.podeVisualizar = podeVisualizar;
        this.podeInserir = podeInserir;
        this.podeEditar = podeEditar;
        this.podeExcluir = podeExcluir;
    }
    
   
    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Permissao getPermissao() {
        return permissao;
    }

    public void setPermissao(Permissao permissao) {
        this.permissao = permissao;
    }

    public boolean isPodeVisualizar() {
        return podeVisualizar;
    }

    public void setPodeVisualizar(boolean podeVisualizar) {
        this.podeVisualizar = podeVisualizar;
    }

    public boolean isPodeInserir() {
        return podeInserir;
    }

    public void setPodeInserir(boolean podeInserir) {
        this.podeInserir = podeInserir;
    }

    public boolean isPodeEditar() {
        return podeEditar;
    }

    public void setPodeEditar(boolean podeEditar) {
        this.podeEditar = podeEditar;
    }

    public boolean isPodeExcluir() {
        return podeExcluir;
    }

    public void setPodeExcluir(boolean podeExcluir) {
        this.podeExcluir = podeExcluir;
    }

}