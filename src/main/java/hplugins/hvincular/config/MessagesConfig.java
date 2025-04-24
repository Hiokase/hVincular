package hplugins.hvincular.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import hplugins.hvincular.HVincular;
import hplugins.hvincular.util.ColorUtils;
import hplugins.hvincular.util.ConfigUtil;

import java.io.File;
import java.io.IOException;

/**
 * Gerenciador responsável pelas mensagens do plugin.
 */
public class MessagesConfig {

    private final HVincular plugin;
    private final File messagesFile;
    private FileConfiguration messagesConfig;
    
    private String prefix;
    private String noPermission;
    private String playerOnlyCommand;
    private String enterYouTubeLink;
    private String invalidLink;
    private String verificationStarted;
    private String verificationSuccess;
    private String verificationFailed;
    private String insufficientSubscribers;
    private String insufficientViews;
    private String missingServerIp;
    private String missingVerificationCode;
    private String cooldownActive;
    private String tagAlreadyOwned;
    private String videoAlreadyUsed;
    private String verificationCodeInfo;
    private String commandRequired;
    private String selectTagFirst;
    
    private String desvincularUsage;
    private String desvincularPlayerNotFound;
    private String desvincularPlayerOffline;
    private String desvincularTagNotFound;
    private String desvincularSuccessSingle;
    private String desvincularSuccessAll;
    private String desvincularNoTags;
    private String desvincularError;
    
    private String consoleDesvincularTagSuccess;
    private String consoleDesvincularAllSuccess;
    private String consoleDesvincularTagError;
    private String consoleDesvincularAllError;
    
    private String commandUnknown;
    private String reloadSuccess;
    private String reloadConsoleByPlayer;
    private String reloadConsoleByConsole;
    private String reloadReloadingSounds;
    private String reloadError;
    private String reloadErrorConsole;
    private String helpHeader;
    private String helpVincular;
    private String helpVincularUrl;
    private String helpHvHelp;
    private String helpHvReload;
    private String helpFooter;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public MessagesConfig(HVincular plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        createMessagesFile();
    }

    /**
     * Cria o arquivo de mensagens caso não exista.
     */
    private void createMessagesFile() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    /**
     * Carrega ou recarrega as mensagens do plugin.
     * Usa UTF-8 para garantir que caracteres especiais sejam lidos corretamente.
     */
    public void loadMessages() {
        messagesConfig = ConfigUtil.loadConfiguration(plugin, messagesFile, "messages.yml");
        loadMessageValues();
    }

    /**
     * Carrega os valores de mensagens do arquivo.
     */
    private void loadMessageValues() {
        prefix = ColorUtils.translate(messagesConfig.getString("prefix", "&8[&6hVincular&8] &r"));
        noPermission = formatMessage(messagesConfig.getString("no-permission", "&cVocê não tem permissão para usar este comando."));
        playerOnlyCommand = formatMessage(messagesConfig.getString("player-only-command", "&cEste comando só pode ser executado por jogadores."));
        enterYouTubeLink = formatMessage(messagesConfig.getString("enter-youtube-link", "&eDigite o link do seu vídeo do YouTube no chat:"));
        invalidLink = formatMessage(messagesConfig.getString("invalid-link", "&cO link fornecido não é válido. Certifique-se de enviar um link do YouTube."));
        verificationStarted = formatMessage(messagesConfig.getString("verification-started", "&eVerificando seu canal do YouTube, aguarde..."));
        verificationSuccess = formatMessage(messagesConfig.getString("verification-success", "&aVerificação concluída com sucesso! A tag foi aplicada."));
        verificationFailed = formatMessage(messagesConfig.getString("verification-failed", "&cA verificação falhou. Verifique os requisitos e tente novamente."));
        insufficientSubscribers = formatMessage(messagesConfig.getString("insufficient-subscribers", "&cSeu canal não possui o número mínimo de inscritos necessário (%subscribers% necessários)."));
        insufficientViews = formatMessage(messagesConfig.getString("insufficient-views", "&cSeu vídeo não possui o número mínimo de visualizações necessárias (%views% necessárias)."));
        missingServerIp = formatMessage(messagesConfig.getString("missing-server-ip", "&cO IP do servidor não foi encontrado na descrição do vídeo."));
        missingVerificationCode = formatMessage(messagesConfig.getString("missing-verification-code", "&cSeu código de verificação '%code%' não foi encontrado na descrição do vídeo. Certifique-se de adicionar exatamente o código mostrado abaixo da mensagem de verificação."));
        cooldownActive = formatMessage(messagesConfig.getString("cooldown-active", "&cVocê precisa aguardar %time% antes de solicitar esta tag novamente."));
        tagAlreadyOwned = formatMessage(messagesConfig.getString("tag-already-owned", "&cVocê já possui esta tag! Não é possível vincular uma tag que você já tem."));
        videoAlreadyUsed = formatMessage(messagesConfig.getString("video-already-used", "&cEste vídeo já foi usado por outro jogador para obter uma tag. Você precisa usar um vídeo diferente."));
        verificationCodeInfo = formatMessage(messagesConfig.getString("verification-code-info", "&eVocê deve adicionar o seguinte código na descrição do seu vídeo:\n&b%code%\n&eJunto com o IP do servidor."));
        commandRequired = formatMessage(messagesConfig.getString("command-required", "&eDetectamos um link do YouTube! Use o comando &6/vincular &epara iniciar o processo de verificação."));
        selectTagFirst = formatMessage(messagesConfig.getString("select-tag-first", "&ePrimeiro escolha uma tag que deseja vincular ao seu canal do YouTube."));
        
        desvincularUsage = formatMessage(messagesConfig.getString("desvincular-usage", "&cUso: /hdesvincular <jogador> [tag]"));
        desvincularPlayerNotFound = formatMessage(messagesConfig.getString("desvincular-player-not-found", "&cJogador não encontrado: %player%"));
        desvincularPlayerOffline = formatMessage(messagesConfig.getString("desvincular-player-offline", "&eO jogador está offline. Para desvincular tags, o jogador precisa estar online."));
        desvincularTagNotFound = formatMessage(messagesConfig.getString("desvincular-tag-not-found", "&cTag não encontrada: %tag%"));
        desvincularSuccessSingle = formatMessage(messagesConfig.getString("desvincular-success-single", "&aTag %tag% desvinculada com sucesso do jogador %player%"));
        desvincularSuccessAll = formatMessage(messagesConfig.getString("desvincular-success-all", "&a%count% tags desvinculadas com sucesso do jogador %player%"));
        desvincularNoTags = formatMessage(messagesConfig.getString("desvincular-no-tags", "&eO jogador %player% não possui tags vinculadas."));
        desvincularError = formatMessage(messagesConfig.getString("desvincular-error", "&cErro ao desvincular tag(s): %error%"));
        
        consoleDesvincularTagSuccess = ColorUtils.translate(messagesConfig.getString("console-desvincular-tag-success", "&eAdmin %admin% desvinculou a tag %tag% do jogador %player%"));
        consoleDesvincularAllSuccess = ColorUtils.translate(messagesConfig.getString("console-desvincular-all-success", "&eAdmin %admin% desvinculou %count% tags do jogador %player%"));
        consoleDesvincularTagError = ColorUtils.translate(messagesConfig.getString("console-desvincular-tag-error", "&cErro ao desvincular tag %tag% do jogador %player%: %error%"));
        consoleDesvincularAllError = ColorUtils.translate(messagesConfig.getString("console-desvincular-all-error", "&cErro ao desvincular todas as tags do jogador %player%: %error%"));
        
        commandUnknown = formatMessage(messagesConfig.getString("command-unknown", "&cComando desconhecido. Use &e/hv help &cpara ver os comandos disponíveis."));
        reloadSuccess = formatMessage(messagesConfig.getString("reload-success", "&a&lPlugin recarregado com sucesso!"));
        reloadConsoleByPlayer = ColorUtils.translate(messagesConfig.getString("reload-console-by-player", "&aPlugin recarregado por %player%"));
        reloadConsoleByConsole = ColorUtils.translate(messagesConfig.getString("reload-console-by-console", "&aPlugin recarregado pelo console"));
        reloadReloadingSounds = ColorUtils.translate(messagesConfig.getString("reload-reloading-sounds", "&aRecarregando configurações de sons..."));
        reloadError = formatMessage(messagesConfig.getString("reload-error", "&c&lErro ao recarregar o plugin. Verifique o console para mais detalhes."));
        reloadErrorConsole = ColorUtils.translate(messagesConfig.getString("reload-error-console", "&cErro ao recarregar o plugin: %error%"));
        helpHeader = formatMessage(messagesConfig.getString("help-header", "&e&l=== Comandos Disponíveis ==="));
        helpVincular = formatMessage(messagesConfig.getString("help-vincular", "&e/vincular &8- &7Abre o menu de vinculação de tags"));
        helpVincularUrl = formatMessage(messagesConfig.getString("help-vincular-url", "&e/vincular <url> &8- &7Vincula diretamente uma URL do YouTube"));
        helpHvHelp = formatMessage(messagesConfig.getString("help-hv-help", "&e/hv help &8- &7Mostra esta ajuda"));
        helpHvReload = formatMessage(messagesConfig.getString("help-hv-reload", "&e/hv reload &8- &7Recarrega as configurações do plugin"));
        helpFooter = formatMessage(messagesConfig.getString("help-footer", "&e&l=== Versão: %version% ==="));
    }

    /**
     * Formata a mensagem adicionando o prefixo.
     * @param message Mensagem a ser formatada
     * @return Mensagem formatada
     */
    private String formatMessage(String message) {
        return ColorUtils.translate(prefix + message);
    }

    /**
     * Salva as mensagens no arquivo usando UTF-8.
     */
    public void saveMessages() {
        ConfigUtil.saveConfiguration(plugin, messagesConfig, messagesFile);
    }

    
    public String getNoPermission() {
        return noPermission;
    }

    public String getPlayerOnlyCommand() {
        return playerOnlyCommand;
    }

    public String getEnterYouTubeLink() {
        return enterYouTubeLink;
    }

    public String getInvalidLink() {
        return invalidLink;
    }

    public String getVerificationStarted() {
        return verificationStarted;
    }

    public String getVerificationSuccess() {
        return verificationSuccess;
    }

    public String getVerificationFailed() {
        return verificationFailed;
    }

    public String getInsufficientSubscribers(int required) {
        return insufficientSubscribers.replace("%subscribers%", String.valueOf(required));
    }

    public String getMissingServerIp() {
        return missingServerIp;
    }
    
    /**
     * Retorna a mensagem de visualizações insuficientes.
     * @param required Quantidade de visualizações necessárias
     * @return Mensagem formatada com a quantidade necessária
     */
    public String getInsufficientViews(int required) {
        return insufficientViews.replace("%views%", String.valueOf(required));
    }
    
    /**
     * Retorna a mensagem de cooldown ativo.
     * @param timeRemaining Tempo restante formatado
     * @return Mensagem formatada com o tempo restante
     */
    public String getCooldownActive(String timeRemaining) {
        return cooldownActive.replace("%time%", timeRemaining);
    }
    
    /**
     * Retorna a mensagem de tag já possuída.
     * @return Mensagem informando que o jogador já possui a tag
     */
    public String getTagAlreadyOwned() {
        return tagAlreadyOwned;
    }
    
    /**
     * Retorna a mensagem de vídeo já utilizado por outro jogador.
     * @return Mensagem informando que o vídeo já foi usado por outro jogador
     */
    public String getVideoAlreadyUsed() {
        return videoAlreadyUsed;
    }
    
    /**
     * Retorna a mensagem informando que o código de verificação não foi encontrado.
     * @param code Código de verificação que deveria estar na descrição
     * @return Mensagem formatada com o código de verificação
     */
    public String getMissingVerificationCode(String code) {
        return missingVerificationCode.replace("%code%", code);
    }
    
    /**
     * Retorna a mensagem com as instruções para adicionar o código de verificação.
     * @param code Código de verificação que deve ser adicionado na descrição
     * @return Mensagem formatada com o código de verificação
     */
    public String getVerificationCodeInfo(String code) {
        return verificationCodeInfo.replace("%code%", code);
    }
    
    /**
     * Retorna a mensagem informando que é necessário usar o comando /vincular.
     * @return Mensagem orientando o jogador a usar o comando
     */
    public String getCommandRequired() {
        return commandRequired;
    }
    
    /**
     * Retorna a mensagem informando que o jogador precisa selecionar uma tag primeiro.
     * @return Mensagem orientando o jogador a selecionar uma tag
     */
    public String getSelectTagFirst() {
        return selectTagFirst;
    }
    
    
    /**
     * Retorna a mensagem de uso incorreto do comando de desvinculação.
     * @return Mensagem de uso do comando
     */
    public String getDesvincularUsage() {
        return desvincularUsage;
    }
    
    /**
     * Retorna a mensagem de jogador não encontrado.
     * @param playerName Nome do jogador
     * @return Mensagem formatada com o nome do jogador
     */
    public String getDesvincularPlayerNotFound(String playerName) {
        return desvincularPlayerNotFound.replace("%player%", playerName);
    }
    
    /**
     * Retorna a mensagem informando que o jogador está offline.
     * @return Mensagem informando que o jogador está offline
     */
    public String getDesvincularPlayerOffline() {
        return desvincularPlayerOffline;
    }
    
    /**
     * Retorna a mensagem de tag não encontrada.
     * @param tagId ID ou nome da tag
     * @return Mensagem formatada com o ID/nome da tag
     */
    public String getDesvincularTagNotFound(String tagId) {
        return desvincularTagNotFound.replace("%tag%", tagId);
    }
    
    /**
     * Retorna a mensagem de sucesso ao desvincular uma tag específica.
     * @param tagName Nome da tag
     * @param playerName Nome do jogador
     * @return Mensagem formatada com o nome da tag e do jogador
     */
    public String getDesvincularSuccessSingle(String tagName, String playerName) {
        return desvincularSuccessSingle
                .replace("%tag%", tagName)
                .replace("%player%", playerName);
    }
    
    /**
     * Retorna a mensagem de sucesso ao desvincular várias tags.
     * @param count Número de tags desvinculadas
     * @param playerName Nome do jogador
     * @return Mensagem formatada com o número de tags e o nome do jogador
     */
    public String getDesvincularSuccessAll(int count, String playerName) {
        return desvincularSuccessAll
                .replace("%count%", String.valueOf(count))
                .replace("%player%", playerName);
    }
    
    /**
     * Retorna a mensagem informando que o jogador não possui tags vinculadas.
     * @param playerName Nome do jogador
     * @return Mensagem formatada com o nome do jogador
     */
    public String getDesvincularNoTags(String playerName) {
        return desvincularNoTags.replace("%player%", playerName);
    }
    
    /**
     * Retorna a mensagem de erro ao desvincular tag(s).
     * @param error Mensagem de erro
     * @return Mensagem formatada com o erro
     */
    public String getDesvincularError(String error) {
        return desvincularError.replace("%error%", error);
    }
    
    
    /**
     * Retorna a mensagem de console para sucesso ao desvincular uma tag específica.
     * @param admin Nome do administrador que executou o comando
     * @param tagName Nome da tag desvinculada
     * @param playerName Nome do jogador
     * @return Mensagem formatada para o console
     */
    public String getConsoleDesvincularTagSuccess(String admin, String tagName, String playerName) {
        return consoleDesvincularTagSuccess
                .replace("%admin%", admin)
                .replace("%tag%", tagName)
                .replace("%player%", playerName);
    }
    
    /**
     * Retorna a mensagem de console para sucesso ao desvincular todas as tags.
     * @param admin Nome do administrador que executou o comando
     * @param count Número de tags desvinculadas
     * @param playerName Nome do jogador
     * @return Mensagem formatada para o console
     */
    public String getConsoleDesvincularAllSuccess(String admin, int count, String playerName) {
        return consoleDesvincularAllSuccess
                .replace("%admin%", admin)
                .replace("%count%", String.valueOf(count))
                .replace("%player%", playerName);
    }
    
    /**
     * Retorna a mensagem de console para erro ao desvincular uma tag específica.
     * @param tagId ID da tag
     * @param playerName Nome do jogador
     * @param error Mensagem de erro
     * @return Mensagem formatada para o console
     */
    public String getConsoleDesvincularTagError(String tagId, String playerName, String error) {
        return consoleDesvincularTagError
                .replace("%tag%", tagId)
                .replace("%player%", playerName)
                .replace("%error%", error);
    }
    
    /**
     * Retorna a mensagem de console para erro ao desvincular todas as tags.
     * @param playerName Nome do jogador
     * @param error Mensagem de erro
     * @return Mensagem formatada para o console
     */
    public String getConsoleDesvincularAllError(String playerName, String error) {
        return consoleDesvincularAllError
                .replace("%player%", playerName)
                .replace("%error%", error);
    }
    
    
    /**
     * Retorna a mensagem de comando desconhecido.
     * @return Mensagem informando que o comando é desconhecido
     */
    public String getCommandUnknown() {
        return commandUnknown;
    }
    
    /**
     * Retorna a mensagem de sucesso ao recarregar o plugin.
     * @return Mensagem informando que o plugin foi recarregado com sucesso
     */
    public String getReloadSuccess() {
        return reloadSuccess;
    }
    
    /**
     * Retorna a mensagem de console para o plugin recarregado por um jogador.
     * @param playerName Nome do jogador que recarregou o plugin
     * @return Mensagem formatada para o console
     */
    public String getReloadConsoleByPlayer(String playerName) {
        return reloadConsoleByPlayer.replace("%player%", playerName);
    }
    
    /**
     * Retorna a mensagem de console para o plugin recarregado pelo console.
     * @return Mensagem para o console
     */
    public String getReloadConsoleByConsole() {
        return reloadConsoleByConsole;
    }
    
    /**
     * Retorna a mensagem de recarregamento das configurações de sons.
     * @return Mensagem informando que as configurações de sons estão sendo recarregadas
     */
    public String getReloadReloadingSounds() {
        return reloadReloadingSounds;
    }
    
    /**
     * Retorna a mensagem de erro ao recarregar o plugin.
     * @return Mensagem informando que houve um erro ao recarregar o plugin
     */
    public String getReloadError() {
        return reloadError;
    }
    
    /**
     * Retorna a mensagem de console para erro ao recarregar o plugin.
     * @param error Mensagem de erro
     * @return Mensagem formatada para o console
     */
    public String getReloadErrorConsole(String error) {
        return reloadErrorConsole.replace("%error%", error);
    }
    
    /**
     * Retorna o cabeçalho da ajuda.
     * @return Mensagem com o cabeçalho da ajuda
     */
    public String getHelpHeader() {
        return helpHeader;
    }
    
    /**
     * Retorna a mensagem de ajuda para o comando vincular.
     * @return Mensagem com a ajuda do comando vincular
     */
    public String getHelpVincular() {
        return helpVincular;
    }
    
    /**
     * Retorna a mensagem de ajuda para o comando vincular com URL.
     * @return Mensagem com a ajuda do comando vincular com URL
     */
    public String getHelpVincularUrl() {
        return helpVincularUrl;
    }
    
    /**
     * Retorna a mensagem de ajuda para o comando hv help.
     * @return Mensagem com a ajuda do comando hv help
     */
    public String getHelpHvHelp() {
        return helpHvHelp;
    }
    
    /**
     * Retorna a mensagem de ajuda para o comando hv reload.
     * @return Mensagem com a ajuda do comando hv reload
     */
    public String getHelpHvReload() {
        return helpHvReload;
    }
    
    /**
     * Retorna o rodapé da ajuda.
     * @param version Versão do plugin
     * @return Mensagem com o rodapé da ajuda formatado com a versão
     */
    public String getHelpFooter(String version) {
        return helpFooter.replace("%version%", version);
    }
}
