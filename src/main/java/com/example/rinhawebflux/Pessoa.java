package com.example.rinhawebflux;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Pessoa implements Serializable {
    private UUID id = UUID.randomUUID();
    private String nome;
    private String apelido;
    private LocalDate nascimento;
    private String[] stack;

    public Pessoa() {
    }

    public Pessoa(String nome, String apelido, LocalDate nascimento, String[] stack) {
        this.nome = nome;
        this.apelido = apelido;
        this.nascimento = nascimento;
        this.stack = stack;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public LocalDate getNascimento() {
        return nascimento;
    }

    public void setNascimento(LocalDate nascimento) {
        this.nascimento = nascimento;
    }

    public String[] getStack() {
        return stack;
    }

    public void setStack(String[] stack) {
        this.stack = stack;
    }

    public boolean isValid() { //return 422
        if (this.nome == null || this.apelido == null) return false;
        return this.nome.length() <= 100 && this.apelido.length() <= 32;
    }

    public boolean isTypeValid() { //return 400
        try {
            if (this.nome == null) return false;
            Long.parseLong(this.nome);
            return false;
        } catch (Exception error) {
            //ignore error
        }

        if (this.stack == null) {
            this.setStack(new String[0]);
            return true;
        };

        for (String stack : this.stack) {
            if (stack == null) return false;
            if (stack.length() > 32) return false;

            try {
                Long.parseLong(stack);
                return false;
            } catch (Exception error) {
                //ignore error
            }
        }

        return true;
    }
}
