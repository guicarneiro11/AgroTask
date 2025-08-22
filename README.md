# AgroTask - Sistema de Gestão Agrícola

App Android nativo desenvolvido em Kotlin com Jetpack Compose para gerenciamento de tarefas agrícolas com funcionamento offline e integração com API de clima.

## Como executar

### Pré-requisitos
- Android Studio
- JDK 11+
- Dispositivo/Emulador com Android 7.0+ (API 24)

### Configuração

1. **Clone o repositório**
```bash
git clone https://github.com/guicarneiro11/AgroTask.git
```

2. **Configure a API de Clima**

Crie um arquivo `local.properties` na raiz do projeto com sua chave da [WeatherAPI](https://www.weatherapi.com/):

```properties
WEATHER_API_KEY=sua_chave_aqui
```

> Para obter uma chave gratuita, cadastre-se em https://www.weatherapi.com/

3. **Abra no Android Studio e execute**

O projeto já inclui o `google-services.json` configurado para Firebase (Firestore Database).

## Funcionalidades

### 1. Tarefas do Dia (Offline)
- Lista tarefas com status (Pendente/Em Andamento/Concluída)
- Criação e edição de tarefas
- Persistência local com Room
- Sincronização automática com Firebase quando online

### 2. Registro de Atividades (Offline)
- Formulário com tipos de atividade predefinidos
- Registro de horário início/fim
- Campo de observações
- Histórico completo de atividades

### 3. Previsão do Tempo (Online + Cache)
- Temperatura e umidade atual
- Previsão das próximas 24 horas
- Ícones representativos das condições
- Cache local da última consulta para uso offline
- Indicador visual de dados online/cache

## Arquitetura

### Kotlin Multiplatform (KMP)
```
composeApp/
├── commonMain/    
│   ├── data/    
│   ├── domain/      
│   └── presentation/     
└── androidMain/     
    ├── data/  
    └── di/      
```

### Padrões utilizados
- **MVVM**
- **Repository Pattern**
- **Clean Architecture**
- **Injeção de Dependências**

## Tecnologias
- **UI**: Jetpack Compose
- **Persistência**: Room Database
- **Sincronização**: Firebase Firestore
- **Rede**: Ktor Client
- **DI**: Koin
- **Async**: Coroutines + Flow
- **Serialização**: Kotlinx Serialization
- **Testes**: 8 testes automatizados