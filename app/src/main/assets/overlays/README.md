# Overlays de Entidades

Esta pasta contém as imagens de overlay que serão sobrepostas às fotos dos usuários.

## Estrutura de Nomes

Os arquivos podem seguir um dos seguintes padrões:

### Formato 1 (com underscore):
```
{entityId}_{cameraType}.png
```

### Formato 2 (sem underscore):
```
{entityId}{cameraType}.png
```

Onde:
- `{entityId}` = ID da entidade (ex: "legiao")
- `{cameraType}` = "frontal" ou "traseira"

## Exemplos

**Com underscore:**
- `legiao_frontal.png` - Overlay para câmera frontal da entidade Legião
- `legiao_traseira.png` - Overlay para câmera traseira da entidade Legião

**Sem underscore:**
- `legiaofrontal.png` - Overlay para câmera frontal da entidade Legião
- `legiaotraseira.png` - Overlay para câmera traseira da entidade Legião

**Ambos os formatos funcionam!** O sistema tentará automaticamente as variações.

## Fallback

Se nenhum overlay específico for encontrado, o sistema tentará usar:
- `default_frontal.png`
- `default_traseira.png`

Se ainda assim não encontrar, aplicará apenas efeitos de terror básicos.

## Formato Recomendado

- Formato: PNG com transparência (alpha channel)
- Tamanho: Qualquer (será redimensionado automaticamente)
- Transparência: Use alpha channel para permitir visualizar a foto abaixo
- Recomendação: Imagens semi-transparentes para melhor efeito de sobreposição

