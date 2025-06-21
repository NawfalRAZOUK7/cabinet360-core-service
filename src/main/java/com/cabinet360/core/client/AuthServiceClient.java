package com.cabinet360.core.client;

import com.cabinet360.core.dto.AssistantDto;
import com.cabinet360.core.dto.MedecinDto;
import com.cabinet360.core.dto.PatientDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public class AuthServiceClient {

    private final WebClient webClient;

    @Autowired
    public AuthServiceClient(WebClient authServiceWebClient) {
        this.webClient = authServiceWebClient;
    }

    /**
     * Récupère la liste des médecins depuis auth-service avec filtres optionnels.
     *
     * @param specialite filtre spécialité (ex: "cardiologie"), facultatif
     * @param isAvailable filtre disponibilité (true/false), facultatif
     * @return liste de MedecinDto enrichis
     */
    public List<MedecinDto> getMedecinsByFilter(Optional<String> specialite, Optional<Boolean> isAvailable) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/doctors")
                        .queryParamIfPresent("specialite", specialite)
                        .queryParamIfPresent("isAvailable", isAvailable)
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("Erreur lors de l'appel à auth-service")))
                .bodyToFlux(MedecinDto.class)
                .collectList()
                .block();  // Pour appel synchrone, adapte en Mono<List<MedecinDto>> si tu veux
    }

    /**
     * Récupère la liste des patients depuis auth-service avec filtres optionnels.
     *
     * @param nom filtre par nom (partiel), facultatif
     * @param actif filtre par statut actif (true/false), facultatif
     * @return liste de PatientDto enrichis
     */
    public List<PatientDto> getPatientsByFilter(String nom, Boolean actif) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/admin/patients");
                    if (nom != null && !nom.isBlank()) builder.queryParam("nom", nom);
                    if (actif != null) builder.queryParam("actif", actif);
                    return builder.build();
                })
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("Erreur lors de l'appel à auth-service")))
                .bodyToFlux(PatientDto.class)
                .collectList()
                .block();  // Pour appel synchrone, adapte en Mono<List<PatientDto>> si tu veux
    }

    /**
     * Récupère la liste des assistants depuis auth-service avec filtres optionnels.
     *
     * @param nom filtre par nom (partiel), facultatif
     * @param actif filtre par statut actif (true/false), facultatif
     * @return liste d’AssistantDto
     */
    public List<AssistantDto> getAssistantsByFilter(String nom, Boolean actif) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/admin/assistants");
                    if (nom != null && !nom.isBlank()) builder.queryParam("nom", nom);
                    if (actif != null) builder.queryParam("actif", actif);
                    return builder.build();
                })
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("Erreur lors de l'appel à auth-service")))
                .bodyToFlux(AssistantDto.class)
                .collectList()
                .block();
    }

    // Tu peux ajouter d'autres méthodes pour d'autres endpoints de l'auth-service
}
