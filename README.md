🏫 Sistema de Gerenciamento de Creche
Sistema completo desenvolvido em Java para gerenciamento de creches, controlando alunos, funcionários, responsáveis, turmas e matrículas com interface gráfica intuitiva e persistência em banco de dados MySQL.

📋 Índice
Funcionalidades

Pré-requisitos

Instalação e Configuração

Estrutura do Banco de Dados

Como Usar

Arquitetura do Sistema

Tecnologias Utilizadas

Estrutura do Projeto

🎯 Funcionalidades
Gestão Completa de Alunos
✅ Cadastro completo com dados pessoais, saúde e documentação

✅ Controle de necessidades especiais e classificações

✅ Sistema de irmãos na creche

✅ Associação com múltiplos responsáveis

✅ Dados socioeconômicos completos

Sistema de Matrículas
✅ Fluxo completo: Pré-matrícula → Ativação

✅ Controle de situações (ATIVA, PENDENTE, INATIVA, etc.)

✅ Validação de idade por turma

✅ Declaração de orientações recebidas

✅ Limite de 18 alunos por turma

Gestão de Funcionários
✅ Diferentes tipos: Professor, Assistente, Coordenador

✅ Controle de vínculo e voluntariado

✅ Atribuição específica por função

Sistema de Turmas
✅ Turmas por faixa etária (Creche, Infantil, Pré)

✅ Horários específicos por tipo de turma

✅ Associação com professores

✅ Validação de compatibilidade de idade

Relatórios Completos
✅ Exportação em arquivos TXT

✅ Estatísticas consolidadas

✅ Relatórios específicos por módulo

⚙️ Pré-requisitos
Software Necessário
Java JDK 8 ou superior

MySQL Server 8.0 ou superior

IDE (Eclipse, IntelliJ, VS Code) - opcional

Contas e Acessos
Acesso administrativo ao MySQL

Conexão com internet para download do conector

🚀 Instalação e Configuração
Passo 1: Configurar o Banco de Dados
sql
-- 1. Conecte-se ao MySQL como root ou usuário com privilégios
mysql -u root -p

-- 2. Crie o banco de dados
CREATE DATABASE creche;

-- 3. Use o banco criado
USE creche;

-- 4. Execute o script SQL completo fornecido
-- Copie e cole todo o conteúdo do arquivo SQL fornecido
Passo 2: Baixar o MySQL Connector/J
Opção 1: Download Manual (Recomendado)

Acesse: [https://dev.mysql.com/downloads/connector/j/](https://mvnrepository.com/artifact/com.mysql/mysql-connector-j/9.5.0)

Selecione "Platform Independent"

Baixe o arquivo ZIP

Extraia o arquivo mysql-connector-java-8.0.x.jar

Coloque o JAR na pasta lib do seu projeto

Opção 2: Via Maven (Se estiver usando Maven)

Adicione no pom.xml:

xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
Opção 3: Via linha de comando (Linux/Mac)

bash
# Crie a pasta lib no seu projeto
mkdir lib

# Download direto (Linux/Mac)
wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-8.0.33.zip
unzip mysql-connector-java-8.0.33.zip
cp mysql-connector-java-8.0.33/mysql-connector-java-8.0.33.jar lib/
Passo 3: Configurar a Conexão com o Banco
Localize a classe ConnectionFactory.java e altere a senha:

java
public class ConnectionFactory {
    public Connection recuperarConexao() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/creche", 
                "root",                       // ← Usuário do MySQL
                "SUA_SENHA_AQUI"              // ← ALTERE PARA SUA SENHA
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC não encontrado", e);
        }
    }
}
Configurações comuns:

java
// Para MySQL com senha vazia:
"jdbc:mysql://localhost:3306/creche", "root", ""

// Para MySQL em servidor remoto:
"jdbc:mysql://seudominio.com:3306/creche", "usuario", "senha"

// Para MySQL em porta diferente:
"jdbc:mysql://localhost:3307/creche", "root", "senha"
Passo 4: Configurar o Classpath
No Eclipse:

Botão direito no projeto → Properties

Java Build Path → Libraries → Add JARs

Selecione o mysql-connector-java-8.0.x.jar na pasta lib

No IntelliJ:

File → Project Structure

Modules → Dependencies → ⊕ → JARs or directories

Selecione o arquivo JAR do conector

Compilação via linha de comando:

bash
javac -cp ".:lib/mysql-connector-java-8.0.33.jar" *.java
java -cp ".:lib/mysql-connector-java-8.0.33.jar" SistemaCrecheGUICompleto
🗃️ Estrutura do Banco de Dados
Tabelas Principais
alunos - Dados completos dos alunos

funcionarios - Professores, assistentes e coordenadores

responsaveis - Responsáveis pelos alunos

turmas - Turmas organizadas por faixa etária

matriculas - Controle completo de matrículas

Tabelas de Relacionamento
aluno_turma - Relação muitos-para-muitos

matricula_responsavel - Responsáveis por matrícula

aluno_responsavel - Múltiplos responsáveis por aluno

irmaos - Controle de irmãos na creche

🖥️ Como Usar
Execução do Sistema
bash
# Via IDE: Execute a classe SistemaCrecheGUICompleto
# Via linha de comando:
java -cp ".:lib/mysql-connector-java-8.0.33.jar" SistemaCrecheGUICompleto
Fluxo de Trabalho Recomendado
Cadastrar Funcionários

Professores, Assistentes, Coordenadores

Definir vínculo e turnos

Cadastrar Responsáveis

Dados completos de contato

Parentesco com o aluno

Cadastrar Alunos

Dados pessoais e de saúde

Associar com responsáveis

Criar Turmas

Definir por faixa etária

Atribuir professores

Fazer Pré-matrículas

Associar aluno com responsáveis

Coletar endereço e observações

Ativar Matrículas

Completar dados socioeconômicos

Atribuir turma

Mudar situação para ATIVA

🏗️ Arquitetura do Sistema
Padrões de Projeto Implementados
DAO (Data Access Object) - Isolamento da persistência

Factory Method - Criação de conexões

Template Method - Comportamento comum com especializações

MVC - Separação de concerns

Camadas da Aplicação
text
Apresentação (GUI) 
    ↓
Lógica de Negócio (Domínio) 
    ↓
Persistência (DAO) 
    ↓
Banco de Dados (MySQL)
💻 Tecnologias Utilizadas
Java - Linguagem principal

Swing - Interface gráfica

MySQL - Banco de dados

JDBC - Conexão com banco

Padrões OO - Herança, polimorfismo, encapsulamento

📁 Estrutura do Projeto
text
src/
├── ConnectionFactory.java          # Gerenciamento de conexões
├── SistemaCrecheGUICompleto.java   # Classe principal com GUI
├── DAOs/                           # Camada de persistência
│   ├── AlunoDAO.java
│   ├── FuncionarioDAO.java
│   ├── ResponsavelDAO.java
│   ├── TurmaDAO.java
│   ├── MatriculaDAO.java
│   └── IrmaosDAO.java
├── Models/                         # Entidades de domínio
│   ├── Pessoa.java
│   ├── Aluno.java
│   ├── Funcionario.java
│   │   ├── Professor.java
│   │   ├── Assistente.java
│   │   └── Coordenador.java
│   ├── Responsavel.java
│   ├── Turma.java
│   │   ├── TurmaCreche.java
│   │   ├── TurmaInfantil.java
│   │   └── TurmaPre.java
│   └── Matricula.java
├── Enums/                          # Enumerações
│   ├── SituacaoMatricula.java
│   ├── TipoMoradia.java
│   ├── TipoPiso.java
│   ├── TipoConstrucao.java
│   ├── TipoCobertura.java
│   └── ClassificacaoNecessidadeEspecial.java
└── Components/                     # Componentes customizados
    └── SearchableComboBox.java
🔧 Solução de Problemas
Erros Comuns e Soluções
1. Erro de Driver não encontrado:

Exception: java.lang.ClassNotFoundException: com.mysql.cj.jdbc.Driver
Solução: Verifique se o connector JAR está no classpath.

2. Erro de Acesso negado ao MySQL:

Exception: Access denied for user 'root'@'localhost'
Solução: Verifique usuário e senha no ConnectionFactory.

3. Erro de Banco não existe:

bash
Exception: Unknown database 'creche'
Solução: Execute o script SQL para criar o banco.

4. Erro de Tabela não existe:


Exception: Table 'creche.alunos' doesn't exist
Solução: Verifique se todas as tabelas foram criadas pelo script.

Comandos Úteis MySQL
sql
-- Verificar se o banco foi criado
SHOW DATABASES;

-- Ver tabelas do banco creche
USE creche;
SHOW TABLES;

-- Ver estrutura de uma tabela
DESCRIBE alunos;

-- Ver dados de exemplo
SELECT * FROM alunos LIMIT 5;
📞 Suporte
+55 (98) 981852670
Em caso de problemas:

Verifique se todos os pré-requisitos estão instalados

Confirme que o MySQL está rodando

Valide a senha no ConnectionFactory

Certifique-se que o connector JAR está no classpath

Execute o script SQL completo

📄 Licença
Este projeto foi desenvolvido para fins educacionais como parte de um trabalho acadêmico.
