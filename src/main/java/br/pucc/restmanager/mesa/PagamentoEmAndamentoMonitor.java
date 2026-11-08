package br.pucc.restmanager.mesa;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PagamentoEmAndamentoMonitor implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagamentoEmAndamentoMonitor.class);
    private static final long TEMPO_TOLERANCIA_SEGUNDOS = 60;

    private final MesaPagamentoService pagamentos;
    private final ConcurrentMap<String, Set<String>> sessoesPorOperador = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> geracoesPorOperador = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "pagamento-abandonado-monitor");
        thread.setDaemon(true);
        return thread;
    });

    public PagamentoEmAndamentoMonitor(MesaPagamentoService pagamentos) {
        this.pagamentos = pagamentos;
    }

    public void conectado(String codigoOperador, String sessaoId) {
        if (codigoOperador == null || sessaoId == null) {
            return;
        }
        sessoesPorOperador.compute(codigoOperador, (codigo, sessoes) -> {
            Set<String> sessoesAtivas = sessoes == null
                    ? ConcurrentHashMap.newKeySet()
                    : sessoes;
            sessoesAtivas.add(sessaoId);
            geracoesPorOperador.computeIfAbsent(codigo, ignored -> new AtomicLong())
                    .incrementAndGet();
            return sessoesAtivas;
        });
    }

    public void desconectado(String codigoOperador, String sessaoId) {
        if (codigoOperador == null || sessaoId == null) {
            return;
        }
        boolean[] ultimaSessao = {false};
        sessoesPorOperador.compute(codigoOperador, (codigo, sessoes) -> {
            if (sessoes == null) {
                return null;
            }
            sessoes.remove(sessaoId);
            if (sessoes.isEmpty()) {
                ultimaSessao[0] = true;
                return null;
            }
            return sessoes;
        });
        if (!ultimaSessao[0]) {
            return;
        }

        AtomicLong geracao = geracoesPorOperador.computeIfAbsent(
                codigoOperador, ignored -> new AtomicLong());
        long token = geracao.incrementAndGet();
        executor.schedule(
                () -> liberarSeAindaDesconectado(codigoOperador, token),
                TEMPO_TOLERANCIA_SEGUNDOS,
                TimeUnit.SECONDS);
    }

    private void liberarSeAindaDesconectado(String codigoOperador, long token) {
        AtomicLong geracao = geracoesPorOperador.get(codigoOperador);
        if (geracao == null || geracao.get() != token
                || sessoesPorOperador.containsKey(codigoOperador)) {
            return;
        }
        try {
            pagamentos.cancelarPagamento(codigoOperador);
        } catch (BusinessException ex) {
            if (ex.getErro() != ErrorCode.MESA_NAO_EM_PAGAMENTO) {
                LOGGER.error("Falha ao liberar pagamento abandonado do operador {}", codigoOperador, ex);
            }
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao liberar pagamento abandonado do operador {}", codigoOperador, ex);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}