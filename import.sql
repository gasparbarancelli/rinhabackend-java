CREATE TABLE public.CLIENTE (
                                ID SERIAL PRIMARY KEY,
                                LIMITE INT,
                                SALDO INT DEFAULT 0
) WITH (autovacuum_enabled = false);

CREATE TABLE public.TRANSACAO (
                                  ID SERIAL PRIMARY KEY,
                                  CLIENTE_ID INT NOT NULL,
                                  VALOR INT NOT NULL,
                                  TIPO CHAR(1) NOT NULL,
                                  DESCRICAO VARCHAR(10) NOT NULL,
                                  DATA TIMESTAMP NOT NULL,
                                  FOREIGN KEY (CLIENTE_ID) REFERENCES public.CLIENTE(ID)
) WITH (autovacuum_enabled = false);

INSERT INTO public.CLIENTE (ID, LIMITE)
VALUES (1, 100000),
       (2, 80000),
       (3, 1000000),
       (4, 10000000),
       (5, 500000);

CREATE OR REPLACE FUNCTION efetuar_transacao(
    clienteIdParam int,
    tipoParam varchar(1),
    valorParam int,
    descricaoParam varchar(10)
)
RETURNS TABLE (novoSaldo int, limite int) AS $$
DECLARE
    cliente cliente%rowtype;
    novoSaldo int;
BEGIN

    SELECT * INTO cliente FROM cliente WHERE id = clienteIdParam FOR UPDATE;

    IF tipoParam = 'd' THEN
        IF (cliente.saldo + cliente.limite) < valorParam THEN
            RAISE EXCEPTION 'Cliente nao possui limite';
        END IF;
        novoSaldo := cliente.saldo - valorParam;
    ELSE
        novoSaldo := cliente.saldo + valorParam;
    END IF;

    INSERT INTO transacao (cliente_id, valor, tipo, descricao, data)
    VALUES (clienteIdParam, valorParam, tipoParam, descricaoParam, current_timestamp);

    UPDATE cliente SET saldo = novoSaldo WHERE id = clienteIdParam;

    RETURN QUERY SELECT novoSaldo, cliente.limite;
END;
$$ LANGUAGE plpgsql;