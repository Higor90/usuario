package com.javanauta.usuario.infrastructure.exeptions;

public class ConflictExeption extends RuntimeException{

    public ConflictExeption(String mensagem){
        super(mensagem);
    }
    public ConflictExeption(String mensagem, Throwable throwable){
        super(mensagem);
    }

}
