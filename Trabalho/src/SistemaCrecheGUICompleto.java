import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

// Enums (do segundo código)
enum SituacaoMatricula {
    PRE_MATRICULA, ATIVA, PENDENTE_DADOS, INATIVA, CONCLUIDA, DESISTENTE
}

enum TipoMoradia {
    PROPRIA, CEDIDA, ALUGADA
}

enum TipoPiso {
    CIMENTO, LAJOTA, CHAO_BATIDO
}

enum TipoConstrucao {
    TIJOLO, TAIPA, MADEIRA
}

enum TipoCobertura {
    TELHA, ZINCO, PALHA
}

enum ClassificacaoNecessidadeEspecial {
    ALTAS_HABILIDADES, CEGUEIRA, DEFICIENCIA_AUDITIVA_LEVE, DEFICIENCIA_AUDITIVA_SEVERA,
    DEFICIENCIA_VISUAL, DEFICIENCIA_FISICA_CADEIRANTE, DEFICIENCIA_FISICA_PARALISIA,
    DEFICIENCIA_FISICA_OUTROS, DISFEMIA, DEFICIENCIA_INTELECTUAL, SENSORIAL_ALTA,
    SENSORIAL_BAIXA, DEFICIENCIA_MENTAL, ESPECTRO_AUTISTA_I, ESPECTRO_AUTISTA_II,
    ESPECTRO_AUTISTA_III, ESTRABISMO, SURDO, SINDROME_DOWN, TEA, TDAH, TOD
}

// Classes de persistência (do primeiro código adaptadas)
class ConnectionFactory {
    public Connection recuperarConexao() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/creche", "root", "SUA_SENHA");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC não encontrado", e);
        }
    }
}

// DAOs adaptados para as classes do segundo código
class AlunoDAO {
    private Connection connection;

    public AlunoDAO(Connection connection) {
        this.connection = connection;
    }

    public void cadastrar(Aluno aluno) throws SQLException {
        String sql = "INSERT INTO alunos (nome, data_nascimento, idade, cpf, sexo, cor_raca, problemas_saude, " +
                "gemeo, tem_irmaos_creche, cadastro_sus, unidade_saude, restricao_alimentar, alergia, " +
                "mobilidade_reduzida, deficiencias_multiplas, publico_alvo_especial, auxilio_governo, " +
                "numero_nis, transporte_contratado, serie, ano) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, aluno.getNome());
            stmt.setDate(2, Date.valueOf(aluno.getDataNascimento()));
            stmt.setInt(3, aluno.getIdade());
            stmt.setString(4, aluno.getCpf());
            stmt.setString(5, aluno.getSexo());
            stmt.setString(6, aluno.getCorRaca());
            stmt.setString(7, aluno.getProblemasSaude());
            stmt.setBoolean(8, aluno.isGemeo());
            stmt.setBoolean(9, aluno.isTemIrmaosNaCreche());
            stmt.setString(10, aluno.getCadastroSus());
            stmt.setString(11, aluno.getUnidadeSaude());
            stmt.setString(12, aluno.getRestricaoAlimentar());
            stmt.setString(13, aluno.getAlergia());
            stmt.setString(14, aluno.getMobilidadeReduzida());
            stmt.setBoolean(15, aluno.isDeficienciasMultiplas());
            stmt.setBoolean(16, aluno.isPublicoAlvoEducacaoEspecial());
            stmt.setString(17, aluno.getAuxilioGoverno());
            stmt.setString(18, aluno.getNumeroNis());
            stmt.setBoolean(19, aluno.isTransporteContratado());
            stmt.setString(20, aluno.getSerie());
            stmt.setString(21, aluno.getAno());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    aluno.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Aluno> listarTodos() throws SQLException {
        List<Aluno> alunos = new ArrayList<>();
        String sql = "SELECT * FROM alunos";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Aluno aluno = new Aluno(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDate("data_nascimento").toLocalDate(),
                        rs.getString("cpf"),
                        rs.getString("sexo"),
                        rs.getString("cor_raca"),
                        rs.getString("problemas_saude")
                );

                // Preencher campos adicionais
                aluno.setGemeo(rs.getBoolean("gemeo"));
                aluno.setTemIrmaosNaCreche(rs.getBoolean("tem_irmaos_creche"));
                aluno.setCadastroSus(rs.getString("cadastro_sus"));
                aluno.setUnidadeSaude(rs.getString("unidade_saude"));
                aluno.setRestricaoAlimentar(rs.getString("restricao_alimentar"));
                aluno.setAlergia(rs.getString("alergia"));
                aluno.setMobilidadeReduzida(rs.getString("mobilidade_reduzida"));
                aluno.setDeficienciasMultiplas(rs.getBoolean("deficiencias_multiplas"));
                aluno.setPublicoAlvoEducacaoEspecial(rs.getBoolean("publico_alvo_especial"));
                aluno.setAuxilioGoverno(rs.getString("auxilio_governo"));
                aluno.setNumeroNis(rs.getString("numero_nis"));
                aluno.setTransporteContratado(rs.getBoolean("transporte_contratado"));
                aluno.setSerie(rs.getString("serie"));
                aluno.setAno(rs.getString("ano"));

                alunos.add(aluno);
            }
        }
        return alunos;
    }

    public Aluno buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM alunos WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Aluno aluno = new Aluno(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getDate("data_nascimento").toLocalDate(),
                            rs.getString("cpf"),
                            rs.getString("sexo"),
                            rs.getString("cor_raca"),
                            rs.getString("problemas_saude")
                    );

                    // Preencher campos adicionais
                    aluno.setGemeo(rs.getBoolean("gemeo"));
                    aluno.setTemIrmaosNaCreche(rs.getBoolean("tem_irmaos_creche"));
                    aluno.setCadastroSus(rs.getString("cadastro_sus"));
                    aluno.setUnidadeSaude(rs.getString("unidade_saude"));
                    aluno.setRestricaoAlimentar(rs.getString("restricao_alimentar"));
                    aluno.setAlergia(rs.getString("alergia"));
                    aluno.setMobilidadeReduzida(rs.getString("mobilidade_reduzida"));
                    aluno.setDeficienciasMultiplas(rs.getBoolean("deficiencias_multiplas"));
                    aluno.setPublicoAlvoEducacaoEspecial(rs.getBoolean("publico_alvo_especial"));
                    aluno.setAuxilioGoverno(rs.getString("auxilio_governo"));
                    aluno.setNumeroNis(rs.getString("numero_nis"));
                    aluno.setTransporteContratado(rs.getBoolean("transporte_contratado"));
                    aluno.setSerie(rs.getString("serie"));
                    aluno.setAno(rs.getString("ano"));

                    return aluno;
                }
            }
        }
        return null;
    }
}

class FuncionarioDAO {
    private Connection connection;

    public FuncionarioDAO(Connection connection) {
        this.connection = connection;
    }

    public void cadastrar(Funcionario funcionario) throws SQLException {
        String sql = "INSERT INTO funcionarios (nome, idade, cpf, cargo, vinculo, voluntario, detalhes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, funcionario.getNome());
            stmt.setInt(2, funcionario.getIdade());
            stmt.setString(3, funcionario.getCpf());
            stmt.setString(4, funcionario.getCargo());
            stmt.setString(5, funcionario.getVinculo());
            stmt.setBoolean(6, funcionario.isVoluntario());

            String detalhes = "";
            if (funcionario instanceof Professor) {
                detalhes = ((Professor) funcionario).getTurno();
            } else if (funcionario instanceof Assistente) {
                detalhes = ((Assistente) funcionario).getFaixaEtariaAtendida();
            } else if (funcionario instanceof Coordenador) {
                detalhes = ((Coordenador) funcionario).getSetorResponsavel();
            }
            stmt.setString(7, detalhes);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    funcionario.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Funcionario> listarTodos() throws SQLException {
        List<Funcionario> funcionarios = new ArrayList<>();
        String sql = "SELECT * FROM funcionarios";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Funcionario func = null;
                String cargo = rs.getString("cargo");
                String detalhes = rs.getString("detalhes");

                switch (cargo) {
                    case "Professor":
                        func = new Professor(rs.getInt("id"), rs.getString("nome"),
                                rs.getInt("idade"), rs.getString("cpf"),
                                rs.getString("vinculo"), detalhes, rs.getBoolean("voluntario"));
                        break;
                    case "Assistente":
                        func = new Assistente(rs.getInt("id"), rs.getString("nome"),
                                rs.getInt("idade"), rs.getString("cpf"),
                                rs.getString("vinculo"), detalhes, rs.getBoolean("voluntario"));
                        break;
                    case "Coordenador":
                        func = new Coordenador(rs.getInt("id"), rs.getString("nome"),
                                rs.getInt("idade"), rs.getString("cpf"),
                                rs.getString("vinculo"), detalhes, rs.getBoolean("voluntario"));
                        break;
                }
                if (func != null) {
                    funcionarios.add(func);
                }
            }
        }
        return funcionarios;
    }

    public List<Funcionario> listarPorCargo(String cargo) throws SQLException {
        List<Funcionario> funcionarios = new ArrayList<>();
        String sql = "SELECT * FROM funcionarios WHERE cargo = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cargo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Funcionario func = null;
                    String detalhes = rs.getString("detalhes");

                    switch (cargo) {
                        case "Professor":
                            func = new Professor(rs.getInt("id"), rs.getString("nome"),
                                    rs.getInt("idade"), rs.getString("cpf"),
                                    rs.getString("vinculo"), detalhes, rs.getBoolean("voluntario"));
                            break;
                        case "Assistente":
                            func = new Assistente(rs.getInt("id"), rs.getString("nome"),
                                    rs.getInt("idade"), rs.getString("cpf"),
                                    rs.getString("vinculo"), detalhes, rs.getBoolean("voluntario"));
                            break;
                        case "Coordenador":
                            func = new Coordenador(rs.getInt("id"), rs.getString("nome"),
                                    rs.getInt("idade"), rs.getString("cpf"),
                                    rs.getString("vinculo"), detalhes, rs.getBoolean("voluntario"));
                            break;
                    }
                    if (func != null) {
                        funcionarios.add(func);
                    }
                }
            }
        }
        return funcionarios;
    }

    public Funcionario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM funcionarios WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Funcionario func = null;
                    String cargo = rs.getString("cargo");
                    String detalhes = rs.getString("detalhes");

                    switch (cargo) {
                        case "Professor":
                            func = new Professor(rs.getInt("id"), rs.getString("nome"),
                                    rs.getInt("idade"), rs.getString("cpf"),
                                    rs.getString("vinculo"), detalhes, rs.getBoolean("voluntario"));
                            break;
                        case "Assistente":
                            func = new Assistente(rs.getInt("id"), rs.getString("nome"),
                                    rs.getInt("idade"), rs.getString("cpf"),
                                    rs.getString("vinculo"), detalhes, rs.getBoolean("voluntario"));
                            break;
                        case "Coordenador":
                            func = new Coordenador(rs.getInt("id"), rs.getString("nome"),
                                    rs.getInt("idade"), rs.getString("cpf"),
                                    rs.getString("vinculo"), detalhes, rs.getBoolean("voluntario"));
                            break;
                    }
                    return func;
                }
            }
        }
        return null;
    }
}

class ResponsavelDAO {
    private Connection connection;

    public ResponsavelDAO(Connection connection) {
        this.connection = connection;
    }

    public void cadastrar(Responsavel responsavel) throws SQLException {
        String sql = "INSERT INTO responsaveis (nome, idade, cpf, parentesco, telefone, celular_whatsapp, " +
                "outro_contato, local_trabalho, rg, dias_voluntariado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, responsavel.getNome());
            stmt.setInt(2, responsavel.getIdade());
            stmt.setString(3, responsavel.getCpf());
            stmt.setString(4, responsavel.getParentesco());
            stmt.setString(5, responsavel.getTelefone());
            stmt.setString(6, responsavel.getCelularWhatsapp());
            stmt.setString(7, responsavel.getOutroContato());
            stmt.setString(8, responsavel.getLocalTrabalho());
            stmt.setString(9, responsavel.getRg());
            stmt.setInt(10, responsavel.getDiasVoluntariado());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    responsavel.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Responsavel> listarTodos() throws SQLException {
        List<Responsavel> responsaveis = new ArrayList<>();
        String sql = "SELECT * FROM responsaveis";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Responsavel resp = new Responsavel(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getInt("idade"),
                        rs.getString("cpf"),
                        rs.getString("parentesco"),
                        rs.getString("telefone"),
                        rs.getString("celular_whatsapp")
                );
                resp.setOutroContato(rs.getString("outro_contato"));
                resp.setLocalTrabalho(rs.getString("local_trabalho"));
                resp.setRg(rs.getString("rg"));
                resp.setDiasVoluntariado(rs.getInt("dias_voluntariado"));
                responsaveis.add(resp);
            }
        }
        return responsaveis;
    }

    public Responsavel buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM responsaveis WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Responsavel resp = new Responsavel(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getInt("idade"),
                            rs.getString("cpf"),
                            rs.getString("parentesco"),
                            rs.getString("telefone"),
                            rs.getString("celular_whatsapp")
                    );
                    resp.setOutroContato(rs.getString("outro_contato"));
                    resp.setLocalTrabalho(rs.getString("local_trabalho"));
                    resp.setRg(rs.getString("rg"));
                    resp.setDiasVoluntariado(rs.getInt("dias_voluntariado"));
                    return resp;
                }
            }
        }
        return null;
    }
}

class TurmaDAO {
    private Connection connection;

    public TurmaDAO(Connection connection) {
        this.connection = connection;
    }

    public void cadastrar(Turma turma) throws SQLException {
        String sql = "INSERT INTO turmas (nome, faixa_etaria, turno, tipo, professor_id, detalhes) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, turma.getNome());
            stmt.setString(2, turma.getFaixaEtaria());
            stmt.setString(3, turma.getTurno());

            String tipo = "";
            String detalhes = "";

            if (turma instanceof TurmaCreche) {
                tipo = "CRECHE";
                detalhes = ((TurmaCreche) turma).getHoraCochilo();
            } else if (turma instanceof TurmaInfantil) {
                tipo = "INFANTIL";
                detalhes = ((TurmaInfantil) turma).getHoraCochilo();
            } else if (turma instanceof TurmaPre) {
                tipo = "PRE";
                detalhes = ((TurmaPre) turma).getAulasAlfabetizacao();
            }

            stmt.setString(4, tipo);
            stmt.setInt(5, turma.getProfessor() != null ? turma.getProfessor().getId() : 0);
            stmt.setString(6, detalhes);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    turma.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Turma> listarTodos() throws SQLException {
        List<Turma> turmas = new ArrayList<>();
        String sql = "SELECT t.*, f.nome as professor_nome FROM turmas t LEFT JOIN funcionarios f ON t.professor_id = f.id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Turma turma = null;
                String tipo = rs.getString("tipo");
                String nomeProfessor = rs.getString("professor_nome");
                int professorId = rs.getInt("professor_id");

                // Buscar professor
                Professor professor = null;
                if (nomeProfessor != null) {
                    professor = new Professor(professorId, nomeProfessor, 0, "", "", "", false);
                }

                switch (tipo) {
                    case "CRECHE":
                        turma = new TurmaCreche(rs.getInt("id"), rs.getString("nome"), rs.getString("turno"));
                        break;
                    case "INFANTIL":
                        turma = new TurmaInfantil(rs.getInt("id"), rs.getString("nome"), rs.getString("turno"));
                        break;
                    case "PRE":
                        turma = new TurmaPre(rs.getInt("id"), rs.getString("nome"), rs.getString("turno"), rs.getString("detalhes"));
                        break;
                }

                if (turma != null) {
                    turma.setProfessor(professor);

                    // Carregar alunos da turma
                    List<Aluno> alunosTurma = carregarAlunosTurma(turma.getId());
                    for (Aluno aluno : alunosTurma) {
                        turma.adicionarAluno(aluno);
                    }

                    turmas.add(turma);
                }
            }
        }
        return turmas;
    }

    private List<Aluno> carregarAlunosTurma(int turmaId) throws SQLException {
        List<Aluno> alunos = new ArrayList<>();
        String sql = "SELECT a.* FROM alunos a INNER JOIN aluno_turma at ON a.id = at.aluno_id WHERE at.turma_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, turmaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Aluno aluno = new Aluno(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getDate("data_nascimento").toLocalDate(),
                            rs.getString("cpf"),
                            rs.getString("sexo"),
                            rs.getString("cor_raca"),
                            rs.getString("problemas_saude")
                    );
                    alunos.add(aluno);
                }
            }
        }
        return alunos;
    }

    public void adicionarAlunoTurma(int turmaId, int alunoId) throws SQLException {
        String sql = "INSERT INTO aluno_turma (turma_id, aluno_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, turmaId);
            stmt.setInt(2, alunoId);
            stmt.executeUpdate();
        }
    }
}

class MatriculaDAO {
    private Connection connection;

    public MatriculaDAO(Connection connection) {
        this.connection = connection;
    }

    public void cadastrar(Matricula matricula) throws SQLException {
        String sql = "INSERT INTO matriculas (data, situacao, observacoes, endereco, " +
                "aluno_id, funcionario_id, turma_id, pre_matricula, " +
                "declaracao_orientacoes, data_declaracao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, Date.valueOf(matricula.getData()));
            stmt.setString(2, matricula.getSituacao().name());
            stmt.setString(3, matricula.getObservacoes());
            stmt.setString(4, matricula.getEndereco());
            stmt.setInt(5, matricula.getAluno().getId());
            stmt.setInt(6, matricula.getFuncionario().getId());

            // Usar NULL quando não há turma
            if (matricula.getTurma() != null && matricula.getTurma().getId() != 0) {
                stmt.setInt(7, matricula.getTurma().getId());
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }

            stmt.setBoolean(8, matricula.getSituacao() == SituacaoMatricula.PRE_MATRICULA);
            stmt.setBoolean(9, matricula.isDeclaracaoOrientacoes());
            stmt.setDate(10, matricula.getDataDeclaracao() != null ?
                    Date.valueOf(matricula.getDataDeclaracao()) : null);

            stmt.executeUpdate();

            // Recupera o número gerado pelo banco
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    matricula.setNumeroMatricula(rs.getInt(1));
                }
            }
        }
    }

    public void atualizar(Matricula matricula) throws SQLException {
        String sql = "UPDATE matriculas SET situacao = ?, turma_id = ?, observacoes = ?, endereco = ?, " +
                "pre_matricula = ?, declaracao_orientacoes = ?, data_declaracao = ? WHERE numero = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, matricula.getSituacao().name());

            // Usar NULL quando não há turma
            if (matricula.getTurma() != null && matricula.getTurma().getId() != 0) {
                stmt.setInt(2, matricula.getTurma().getId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }

            stmt.setString(3, matricula.getObservacoes());
            stmt.setString(4, matricula.getEndereco());
            stmt.setBoolean(5, matricula.getSituacao() == SituacaoMatricula.PRE_MATRICULA);
            stmt.setBoolean(6, matricula.isDeclaracaoOrientacoes());
            stmt.setDate(7, matricula.getDataDeclaracao() != null ?
                    Date.valueOf(matricula.getDataDeclaracao()) : null);
            stmt.setInt(8, matricula.getNumeroMatricula());

            stmt.executeUpdate();
        }
    }

    public List<Matricula> listarTodos() throws SQLException {
        List<Matricula> matriculas = new ArrayList<>();
        String sql = "SELECT m.*, a.nome as aluno_nome, f.nome as funcionario_nome, t.nome as turma_nome, " +
                "t.tipo as turma_tipo, t.faixa_etaria as turma_faixa_etaria " +
                "FROM matriculas m " +
                "LEFT JOIN alunos a ON m.aluno_id = a.id " +
                "LEFT JOIN funcionarios f ON m.funcionario_id = f.id " +
                "LEFT JOIN turmas t ON m.turma_id = t.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Criar aluno básico
                Aluno aluno = new Aluno(rs.getInt("aluno_id"), rs.getString("aluno_nome"),
                        LocalDate.now(), "", "", "", "");

                // Criar funcionário básico
                Funcionario funcionario = new Professor(rs.getInt("funcionario_id"),
                        rs.getString("funcionario_nome"), 0, "", "", "", false);

                // Criar turma se existir
                Turma turma = null;
                int turmaId = rs.getInt("turma_id");
                if (!rs.wasNull() && turmaId != 0) {
                    String tipo = rs.getString("turma_tipo");
                    String nome = rs.getString("turma_nome");
                    String faixaEtaria = rs.getString("turma_faixa_etaria");

                    switch (tipo) {
                        case "CRECHE":
                            turma = new TurmaCreche(turmaId, nome, "");
                            break;
                        case "INFANTIL":
                            turma = new TurmaInfantil(turmaId, nome, "");
                            break;
                        case "PRE":
                            turma = new TurmaPre(turmaId, nome, "", "Diárias");
                            break;
                    }
                    if (turma != null) {
                        turma.setFaixaEtaria(faixaEtaria);
                    }
                }

                Matricula matricula = new Matricula(
                        rs.getInt("numero"),
                        rs.getDate("data").toLocalDate(),
                        SituacaoMatricula.valueOf(rs.getString("situacao")),
                        rs.getString("observacoes"),
                        rs.getString("endereco"),
                        aluno,
                        new ArrayList<>(), // Responsáveis serão carregados separadamente
                        funcionario,
                        turma
                );

                matricula.setDeclaracaoOrientacoes(rs.getBoolean("declaracao_orientacoes"));
                if (rs.getDate("data_declaracao") != null) {
                    matricula.setDataDeclaracao(rs.getDate("data_declaracao").toLocalDate());
                }

                // Carregar responsáveis (AGORA COM O MÉTODO CORRETO)
                List<Responsavel> responsaveis = carregarResponsaveisMatricula(rs.getInt("numero"));
                for (Responsavel resp : responsaveis) {
                    matricula.getResponsaveis().add(resp);
                }

                matriculas.add(matricula);
            }
        }
        return matriculas;
    }

    // ADICIONE ESTE MÉTODO (que estava faltando):
    private List<Responsavel> carregarResponsaveisMatricula(int numeroMatricula) throws SQLException {
        List<Responsavel> responsaveis = new ArrayList<>();
        String sql = "SELECT r.* FROM responsaveis r " +
                "INNER JOIN matricula_responsavel mr ON r.id = mr.responsavel_id " +
                "WHERE mr.matricula_numero = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, numeroMatricula);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Responsavel resp = new Responsavel(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getInt("idade"),
                            rs.getString("cpf"),
                            rs.getString("parentesco"),
                            rs.getString("telefone"),
                            rs.getString("celular_whatsapp")
                    );
                    // Carrega campos adicionais
                    resp.setOutroContato(rs.getString("outro_contato"));
                    resp.setLocalTrabalho(rs.getString("local_trabalho"));
                    resp.setRg(rs.getString("rg"));
                    resp.setDiasVoluntariado(rs.getInt("dias_voluntariado"));

                    responsaveis.add(resp);
                }
            }
        }
        return responsaveis;
    }

    public void adicionarResponsavelMatricula(int numeroMatricula, int responsavelId) throws SQLException {
        String sql = "INSERT INTO matricula_responsavel (matricula_numero, responsavel_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, numeroMatricula);
            stmt.setInt(2, responsavelId);
            stmt.executeUpdate();
        }
    }
}

class IrmaosDAO {
    private Connection connection;

    public IrmaosDAO(Connection connection) {
        this.connection = connection;
    }

    public void adicionarIrmaos(int idAluno1, int idAluno2) throws SQLException {
        String sql = "INSERT INTO irmaos (aluno1_id, aluno2_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idAluno1);
            stmt.setInt(2, idAluno2);
            stmt.executeUpdate();
        }
    }

    public boolean saoIrmaos(int idAluno1, int idAluno2) throws SQLException {
        String sql = "SELECT COUNT(*) FROM irmaos WHERE (aluno1_id = ? AND aluno2_id = ?) OR (aluno1_id = ? AND aluno2_id = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idAluno1);
            stmt.setInt(2, idAluno2);
            stmt.setInt(3, idAluno2);
            stmt.setInt(4, idAluno1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Integer> buscarIrmaosPorId(int alunoId) throws SQLException {
        List<Integer> irmaos = new ArrayList<>();
        String sql = "SELECT aluno1_id, aluno2_id FROM irmaos WHERE aluno1_id = ? OR aluno2_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, alunoId);
            stmt.setInt(2, alunoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id1 = rs.getInt("aluno1_id");
                    int id2 = rs.getInt("aluno2_id");
                    if (id1 == alunoId) {
                        irmaos.add(id2);
                    } else {
                        irmaos.add(id1);
                    }
                }
            }
        }
        return irmaos;
    }
}

// Classes de domínio (do segundo código)
abstract class Pessoa {
    protected int id;
    protected String nome;
    protected int idade;
    protected String cpf;

    public Pessoa(int id, String nome, int idade, String cpf) {
        this.id = id;
        this.nome = nome;
        this.idade = idade;
        this.cpf = cpf;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getIdade() { return idade; }
    public String getCpf() { return cpf; }

    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setIdade(int idade) { this.idade = idade; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    @Override
    public String toString() {
        return "ID: " + id + ", Nome: " + nome + ", Idade: " + idade + ", CPF: " + cpf;
    }
}

class Documentacao {
    private String certidaoNascimento;
    private String municipioNascimento;
    private String municipioRegistro;
    private String cartorio;
    private String cpf;
    private String rg;
    private LocalDate dataEmissaoRg;
    private String orgaoEmissor;

    public String getCertidaoNascimento() { return certidaoNascimento; }
    public String getMunicipioNascimento() { return municipioNascimento; }
    public String getMunicipioRegistro() { return municipioRegistro; }
    public String getCartorio() { return cartorio; }
    public String getCpf() { return cpf; }
    public String getRg() { return rg; }
    public LocalDate getDataEmissaoRg() { return dataEmissaoRg; }
    public String getOrgaoEmissor() { return orgaoEmissor; }

    public void setCertidaoNascimento(String certidaoNascimento) { this.certidaoNascimento = certidaoNascimento; }
    public void setMunicipioNascimento(String municipioNascimento) { this.municipioNascimento = municipioNascimento; }
    public void setMunicipioRegistro(String municipioRegistro) { this.municipioRegistro = municipioRegistro; }
    public void setCartorio(String cartorio) { this.cartorio = cartorio; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setRg(String rg) { this.rg = rg; }
    public void setDataEmissaoRg(LocalDate dataEmissaoRg) { this.dataEmissaoRg = dataEmissaoRg; }
    public void setOrgaoEmissor(String orgaoEmissor) { this.orgaoEmissor = orgaoEmissor; }
}

class SituacaoHabitacional {
    private TipoMoradia tipoMoradia;
    private double valorAluguel;
    private int numeroComodos;
    private TipoPiso tipoPiso;
    private TipoConstrucao tipoConstrucao;
    private TipoCobertura cobertura;
    private boolean fossa;
    private boolean cifon;
    private boolean energiaEletrica;
    private boolean aguaEncanada;

    public TipoMoradia getTipoMoradia() { return tipoMoradia; }
    public double getValorAluguel() { return valorAluguel; }
    public int getNumeroComodos() { return numeroComodos; }
    public TipoPiso getTipoPiso() { return tipoPiso; }
    public TipoConstrucao getTipoConstrucao() { return tipoConstrucao; }
    public TipoCobertura getCobertura() { return cobertura; }
    public boolean isFossa() { return fossa; }
    public boolean isCifon() { return cifon; }
    public boolean isEnergiaEletrica() { return energiaEletrica; }
    public boolean isAguaEncanada() { return aguaEncanada; }

    public void setTipoMoradia(TipoMoradia tipoMoradia) { this.tipoMoradia = tipoMoradia; }
    public void setValorAluguel(double valorAluguel) { this.valorAluguel = valorAluguel; }
    public void setNumeroComodos(int numeroComodos) { this.numeroComodos = numeroComodos; }
    public void setTipoPiso(TipoPiso tipoPiso) { this.tipoPiso = tipoPiso; }
    public void setTipoConstrucao(TipoConstrucao tipoConstrucao) { this.tipoConstrucao = tipoConstrucao; }
    public void setCobertura(TipoCobertura cobertura) { this.cobertura = cobertura; }
    public void setFossa(boolean fossa) { this.fossa = fossa; }
    public void setCifon(boolean cifon) { this.cifon = cifon; }
    public void setEnergiaEletrica(boolean energiaEletrica) { this.energiaEletrica = energiaEletrica; }
    public void setAguaEncanada(boolean aguaEncanada) { this.aguaEncanada = aguaEncanada; }
}

class BensFamiliares {
    private boolean tv, dvd, radio, computador, notebook, telefoneFixo;
    private boolean telefoneCelular, tablet, internet, tvAssinatura;
    private boolean fogao, geladeira, freezer, microOndas;
    private boolean maquinaLavar, arCondicionado, bicicleta, moto, automovel;

    public boolean isTv() { return tv; }
    public boolean isDvd() { return dvd; }
    public boolean isRadio() { return radio; }
    public boolean isComputador() { return computador; }
    public boolean isNotebook() { return notebook; }
    public boolean isTelefoneFixo() { return telefoneFixo; }
    public boolean isTelefoneCelular() { return telefoneCelular; }
    public boolean isTablet() { return tablet; }
    public boolean isInternet() { return internet; }
    public boolean isTvAssinatura() { return tvAssinatura; }
    public boolean isFogao() { return fogao; }
    public boolean isGeladeira() { return geladeira; }
    public boolean isFreezer() { return freezer; }
    public boolean isMicroOndas() { return microOndas; }
    public boolean isMaquinaLavar() { return maquinaLavar; }
    public boolean isArCondicionado() { return arCondicionado; }
    public boolean isBicicleta() { return bicicleta; }
    public boolean isMoto() { return moto; }
    public boolean isAutomovel() { return automovel; }

    public void setTv(boolean tv) { this.tv = tv; }
    public void setDvd(boolean dvd) { this.dvd = dvd; }
    public void setRadio(boolean radio) { this.radio = radio; }
    public void setComputador(boolean computador) { this.computador = computador; }
    public void setNotebook(boolean notebook) { this.notebook = notebook; }
    public void setTelefoneFixo(boolean telefoneFixo) { this.telefoneFixo = telefoneFixo; }
    public void setTelefoneCelular(boolean telefoneCelular) { this.telefoneCelular = telefoneCelular; }
    public void setTablet(boolean tablet) { this.tablet = tablet; }
    public void setInternet(boolean internet) { this.internet = internet; }
    public void setTvAssinatura(boolean tvAssinatura) { this.tvAssinatura = tvAssinatura; }
    public void setFogao(boolean fogao) { this.fogao = fogao; }
    public void setGeladeira(boolean geladeira) { this.geladeira = geladeira; }
    public void setFreezer(boolean freezer) { this.freezer = freezer; }
    public void setMicroOndas(boolean microOndas) { this.microOndas = microOndas; }
    public void setMaquinaLavar(boolean maquinaLavar) { this.maquinaLavar = maquinaLavar; }
    public void setArCondicionado(boolean arCondicionado) { this.arCondicionado = arCondicionado; }
    public void setBicicleta(boolean bicicleta) { this.bicicleta = bicicleta; }
    public void setMoto(boolean moto) { this.moto = moto; }
    public void setAutomovel(boolean automovel) { this.automovel = automovel; }
}

class MembroFamilia {
    private String nome;
    private int idade;
    private String parentesco;
    private String situacaoEscolar;
    private String situacaoEmprego;
    private double renda;

    public MembroFamilia(String nome, int idade, String parentesco, String situacaoEscolar, String situacaoEmprego, double renda) {
        this.nome = nome;
        this.idade = idade;
        this.parentesco = parentesco;
        this.situacaoEscolar = situacaoEscolar;
        this.situacaoEmprego = situacaoEmprego;
        this.renda = renda;
    }

    public String getNome() { return nome; }
    public int getIdade() { return idade; }
    public String getParentesco() { return parentesco; }
    public String getSituacaoEscolar() { return situacaoEscolar; }
    public String getSituacaoEmprego() { return situacaoEmprego; }
    public double getRenda() { return renda; }

    public void setNome(String nome) { this.nome = nome; }
    public void setIdade(int idade) { this.idade = idade; }
    public void setParentesco(String parentesco) { this.parentesco = parentesco; }
    public void setSituacaoEscolar(String situacaoEscolar) { this.situacaoEscolar = situacaoEscolar; }
    public void setSituacaoEmprego(String situacaoEmprego) { this.situacaoEmprego = situacaoEmprego; }
    public void setRenda(double renda) { this.renda = renda; }
}

class ComposicaoFamiliar {
    private List<MembroFamilia> membros;
    private double rendaFamiliarTotal;
    private double rendaPerCapita;

    public ComposicaoFamiliar() {
        this.membros = new ArrayList<>();
    }

    public void adicionarMembro(MembroFamilia membro) {
        membros.add(membro);
        calcularRendas();
    }

    private void calcularRendas() {
        rendaFamiliarTotal = membros.stream().mapToDouble(MembroFamilia::getRenda).sum();
        rendaPerCapita = membros.isEmpty() ? 0 : rendaFamiliarTotal / membros.size();
    }

    public List<MembroFamilia> getMembros() { return membros; }
    public double getRendaFamiliarTotal() { return rendaFamiliarTotal; }
    public double getRendaPerCapita() { return rendaPerCapita; }
}

class PessoaAutorizada {
    private String nome;
    private String parentesco;
    private String rg;
    private String telefone;

    public PessoaAutorizada(String nome, String parentesco, String rg, String telefone) {
        this.nome = nome;
        this.parentesco = parentesco;
        this.rg = rg;
        this.telefone = telefone;
    }

    public String getNome() { return nome; }
    public String getParentesco() { return parentesco; }
    public String getRg() { return rg; }
    public String getTelefone() { return telefone; }

    public void setNome(String nome) { this.nome = nome; }
    public void setParentesco(String parentesco) { this.parentesco = parentesco; }
    public void setRg(String rg) { this.rg = rg; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}

class Responsavel extends Pessoa {
    private String parentesco;
    private String telefone;
    private String celularWhatsapp;
    private String outroContato;
    private String localTrabalho;
    private String rg;
    private int diasVoluntariado;

    public Responsavel(int id, String nome, int idade, String cpf, String parentesco,
                       String telefone, String celularWhatsapp) {
        super(id, nome, idade, cpf);
        this.parentesco = parentesco;
        this.telefone = telefone;
        this.celularWhatsapp = celularWhatsapp;
        this.diasVoluntariado = 0;
    }

    public String getParentesco() { return parentesco; }
    public String getTelefone() { return telefone; }
    public String getCelularWhatsapp() { return celularWhatsapp; }
    public String getOutroContato() { return outroContato; }
    public String getLocalTrabalho() { return localTrabalho; }
    public String getRg() { return rg; }
    public int getDiasVoluntariado() { return diasVoluntariado; }

    public void setParentesco(String parentesco) { this.parentesco = parentesco; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setCelularWhatsapp(String celularWhatsapp) { this.celularWhatsapp = celularWhatsapp; }
    public void setOutroContato(String outroContato) { this.outroContato = outroContato; }
    public void setLocalTrabalho(String localTrabalho) { this.localTrabalho = localTrabalho; }
    public void setRg(String rg) { this.rg = rg; }
    public void setDiasVoluntariado(int diasVoluntariado) {
        this.diasVoluntariado = diasVoluntariado;
    }

    @Override
    public String toString() {
        return super.toString() + ", Parentesco: " + parentesco + ", Telefone: " + telefone +
                ", Voluntariado: " + diasVoluntariado + " dias/mês";
    }
}

class Aluno extends Pessoa {
    private LocalDate dataNascimento;
    private String sexo;
    private String corRaca;
    private boolean gemeo;
    private boolean temIrmaosNaCreche;
    private String cadastroSus;
    private String unidadeSaude;
    private String problemasSaude;
    private String restricaoAlimentar;
    private String alergia;
    private String mobilidadeReduzida;
    private boolean deficienciasMultiplas;
    private boolean publicoAlvoEducacaoEspecial;
    private List<ClassificacaoNecessidadeEspecial> classificacoes;
    private String auxilioGoverno;
    private String numeroNis;
    private boolean transporteContratado;
    private List<Responsavel> responsaveis;
    private Responsavel responsavelPrioritario;
    private List<PessoaAutorizada> pessoasAutorizadas;
    private Documentacao documentacao;
    private SituacaoHabitacional situacaoHabitacional;
    private BensFamiliares bens;
    private ComposicaoFamiliar composicaoFamiliar;
    private String serie;
    private String ano;
    private Turma turma;

    public Aluno(int id, String nome, LocalDate dataNascimento, String cpf,
                 String sexo, String corRaca, String problemasSaude) {
        super(id, nome, calcularIdade(dataNascimento), cpf);
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.corRaca = corRaca;
        this.problemasSaude = problemasSaude;
        this.responsaveis = new ArrayList<>();
        this.pessoasAutorizadas = new ArrayList<>();
        this.classificacoes = new ArrayList<>();
        this.composicaoFamiliar = new ComposicaoFamiliar();
        this.documentacao = new Documentacao();
        this.situacaoHabitacional = new SituacaoHabitacional();
        this.bens = new BensFamiliares();
    }

    private static int calcularIdade(LocalDate dataNascimento) {
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getSexo() { return sexo; }
    public String getCorRaca() { return corRaca; }
    public boolean isGemeo() { return gemeo; }
    public boolean isTemIrmaosNaCreche() { return temIrmaosNaCreche; }
    public String getCadastroSus() { return cadastroSus; }
    public String getUnidadeSaude() { return unidadeSaude; }
    public String getProblemasSaude() { return problemasSaude; }
    public String getRestricaoAlimentar() { return restricaoAlimentar; }
    public String getAlergia() { return alergia; }
    public String getMobilidadeReduzida() { return mobilidadeReduzida; }
    public boolean isDeficienciasMultiplas() { return deficienciasMultiplas; }
    public boolean isPublicoAlvoEducacaoEspecial() { return publicoAlvoEducacaoEspecial; }
    public List<ClassificacaoNecessidadeEspecial> getClassificacoes() { return classificacoes; }
    public String getAuxilioGoverno() { return auxilioGoverno; }
    public String getNumeroNis() { return numeroNis; }
    public boolean isTransporteContratado() { return transporteContratado; }
    public List<Responsavel> getResponsaveis() { return responsaveis; }
    public Responsavel getResponsavelPrioritario() { return responsavelPrioritario; }
    public List<PessoaAutorizada> getPessoasAutorizadas() { return pessoasAutorizadas; }
    public Documentacao getDocumentacao() { return documentacao; }
    public SituacaoHabitacional getSituacaoHabitacional() { return situacaoHabitacional; }
    public BensFamiliares getBens() { return bens; }
    public ComposicaoFamiliar getComposicaoFamiliar() { return composicaoFamiliar; }
    public String getSerie() { return serie; }
    public String getAno() { return ano; }
    public Turma getTurma() { return turma; }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
        this.idade = calcularIdade(dataNascimento);
    }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public void setCorRaca(String corRaca) { this.corRaca = corRaca; }
    public void setGemeo(boolean gemeo) { this.gemeo = gemeo; }
    public void setTemIrmaosNaCreche(boolean temIrmaosNaCreche) { this.temIrmaosNaCreche = temIrmaosNaCreche; }
    public void setCadastroSus(String cadastroSus) { this.cadastroSus = cadastroSus; }
    public void setUnidadeSaude(String unidadeSaude) { this.unidadeSaude = unidadeSaude; }
    public void setProblemasSaude(String problemasSaude) { this.problemasSaude = problemasSaude; }
    public void setRestricaoAlimentar(String restricaoAlimentar) { this.restricaoAlimentar = restricaoAlimentar; }
    public void setAlergia(String alergia) { this.alergia = alergia; }
    public void setMobilidadeReduzida(String mobilidadeReduzida) { this.mobilidadeReduzida = mobilidadeReduzida; }
    public void setDeficienciasMultiplas(boolean deficienciasMultiplas) { this.deficienciasMultiplas = deficienciasMultiplas; }
    public void setPublicoAlvoEducacaoEspecial(boolean publicoAlvoEducacaoEspecial) { this.publicoAlvoEducacaoEspecial = publicoAlvoEducacaoEspecial; }
    public void setAuxilioGoverno(String auxilioGoverno) { this.auxilioGoverno = auxilioGoverno; }
    public void setNumeroNis(String numeroNis) { this.numeroNis = numeroNis; }
    public void setTransporteContratado(boolean transporteContratado) { this.transporteContratado = transporteContratado; }
    public void setResponsavelPrioritario(Responsavel responsavelPrioritario) { this.responsavelPrioritario = responsavelPrioritario; }
    public void setSerie(String serie) { this.serie = serie; }
    public void setAno(String ano) { this.ano = ano; }
    public void setTurma(Turma turma) { this.turma = turma; }

    public void adicionarResponsavel(Responsavel responsavel) {
        if (responsaveis.size() < 3) {
            responsaveis.add(responsavel);
            if (responsavelPrioritario == null) {
                responsavelPrioritario = responsavel;
            }
        }
    }

    public void adicionarPessoaAutorizada(PessoaAutorizada pessoa) {
        if (pessoasAutorizadas.size() < 2) {
            pessoasAutorizadas.add(pessoa);
        }
    }

    public void adicionarClassificacao(ClassificacaoNecessidadeEspecial classificacao) {
        classificacoes.add(classificacao);
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Nome: " + nome + ", Data Nasc: " + dataNascimento +
                ", Idade: " + getIdade() + " anos, Sexo: " + sexo;
    }
}

abstract class Funcionario extends Pessoa {
    protected String cargo;
    protected String vinculo;
    protected boolean voluntario;

    public Funcionario(int id, String nome, int idade, String cpf, String cargo, String vinculo, boolean voluntario) {
        super(id, nome, idade, cpf);
        this.cargo = cargo;
        this.vinculo = vinculo;
        this.voluntario = voluntario;
    }

    public String getCargo() { return cargo; }
    public String getVinculo() { return vinculo; }
    public boolean isVoluntario() { return voluntario; }

    public void setCargo(String cargo) { this.cargo = cargo; }
    public void setVinculo(String vinculo) { this.vinculo = vinculo; }
    public void setVoluntario(boolean voluntario) { this.voluntario = voluntario; }

    @Override
    public String toString() {
        return super.toString() + ", Cargo: " + cargo + ", Vínculo: " + vinculo +
                ", " + (voluntario ? "Voluntário" : "Remunerado");
    }
}

class Professor extends Funcionario {
    private Turma turma;
    private String turno;

    public Professor(int id, String nome, int idade, String cpf, String vinculo, String turno, boolean voluntario) {
        super(id, nome, idade, cpf, "Professor", vinculo, voluntario);
        this.turno = turno;
    }

    public Turma getTurma() { return turma; }
    public String getTurno() { return turno; }

    public void setTurma(Turma turma) { this.turma = turma; }
    public void setTurno(String turno) { this.turno = turno; }

    @Override
    public String toString() {
        return super.toString() + ", Turno: " + turno;
    }
}

class Assistente extends Funcionario {
    private String faixaEtariaAtendida;

    public Assistente(int id, String nome, int idade, String cpf, String vinculo,
                      String faixaEtariaAtendida, boolean voluntario) {
        super(id, nome, idade, cpf, "Assistente", vinculo, voluntario);
        this.faixaEtariaAtendida = faixaEtariaAtendida;
    }

    public String getFaixaEtariaAtendida() { return faixaEtariaAtendida; }
    public void setFaixaEtariaAtendida(String faixaEtariaAtendida) { this.faixaEtariaAtendida = faixaEtariaAtendida; }

    @Override
    public String toString() {
        return super.toString() + ", Faixa Etária Atendida: " + faixaEtariaAtendida;
    }
}

class Coordenador extends Funcionario {
    private String setorResponsavel;

    public Coordenador(int id, String nome, int idade, String cpf, String vinculo,
                       String setorResponsavel, boolean voluntario) {
        super(id, nome, idade, cpf, "Coordenador", vinculo, voluntario);
        this.setorResponsavel = setorResponsavel;
    }

    public String getSetorResponsavel() { return setorResponsavel; }
    public void setSetorResponsavel(String setorResponsavel) { this.setorResponsavel = setorResponsavel; }

    @Override
    public String toString() {
        return super.toString() + ", Setor Responsável: " + setorResponsavel;
    }
}

abstract class Turma {
    protected int id;
    protected String nome;
    protected String faixaEtaria;
    protected String turno;
    protected List<Aluno> alunos;
    protected Professor professor;

    public Turma(int id, String nome, String faixaEtaria, String turno) {
        this.id = id;
        this.nome = nome;
        this.faixaEtaria = faixaEtaria;
        this.turno = turno;
        this.alunos = new ArrayList<>();
    }

    public boolean adicionarAluno(Aluno aluno) {
        if (verificarIdadeAluno(aluno.getIdade()) && alunos.size() < 18) {
            alunos.add(aluno);
            aluno.setTurma(this);
            return true;
        }
        return false;
    }

    public abstract boolean verificarIdadeAluno(int idade);

    // GETTERS
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getFaixaEtaria() { return faixaEtaria; }
    public String getTurno() { return turno; }
    public List<Aluno> getAlunos() { return alunos; }
    public Professor getProfessor() { return professor; }

    // SETTERS (ADICIONE ESTES)
    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setFaixaEtaria(String faixaEtaria) { this.faixaEtaria = faixaEtaria; }
    public void setTurno(String turno) { this.turno = turno; }
    public void setProfessor(Professor professor) { this.professor = professor; }

    @Override
    public String toString() {
        return "ID: " + id + ", Nome: " + nome + ", Faixa Etária: " + faixaEtaria +
                ", Turno: " + turno + ", Professor: " + (professor != null ? professor.getNome() : "Não atribuído") +
                ", Alunos: " + alunos.size() + "/18";
    }
}

class TurmaCreche extends Turma {
    private String horaLancheManha = "9:00";
    private String horaAlmoco = "11:00";
    private String horaCochilo = "15:00";
    private String horaBanho = "14:30";
    private String horaSaida = "16:00";

    public TurmaCreche(int id, String nome, String turno) {
        super(id, nome, "2-3 anos", turno);  // ← Chama o construtor da classe pai
    }

    @Override
    public boolean verificarIdadeAluno(int idade) {
        return idade >= 2 && idade <= 3;
    }

    // GETTERS específicos - NÃO sobrescreva getFaixaEtaria()!
    public String getHoraLancheManha() { return horaLancheManha; }
    public String getHoraAlmoco() { return horaAlmoco; }
    public String getHoraCochilo() { return horaCochilo; }
    public String getHoraBanho() { return horaBanho; }
    public String getHoraSaida() { return horaSaida; }

    @Override
    public String toString() {
        return super.toString() + ", Horário: Lanche " + horaLancheManha + ", Almoço " + horaAlmoco +
                ", Cochilo " + horaCochilo;
    }
}


class TurmaInfantil extends Turma {
    private String horaLancheManha = "9:30";
    private String horaAlmoco = "11:40";
    private String horaCochilo = "15:30";
    private String horaBanho = "15:00";
    private String horaSaida = "16:00";

    public TurmaInfantil(int id, String nome, String turno) {
        super(id, nome, "4-5 anos", turno);
    }

    @Override
    public boolean verificarIdadeAluno(int idade) {
        return idade >= 4 && idade <= 5;
    }

    public String getHoraLancheManha() { return horaLancheManha; }
    public String getHoraAlmoco() { return horaAlmoco; }
    public String getHoraCochilo() { return horaCochilo; }
    public String getHoraBanho() { return horaBanho; }
    public String getHoraSaida() { return horaSaida; }

    @Override
    public String toString() {
        return super.toString() + ", Horário: Lanche " + horaLancheManha + ", Almoço " + horaAlmoco +
                ", Cochilo " + horaCochilo;
    }
}

class TurmaPre extends Turma {
    private String aulasAlfabetizacao;

    public TurmaPre(int id, String nome, String turno, String aulasAlfabetizacao) {
        super(id, nome, "6 anos", turno);
        this.aulasAlfabetizacao = aulasAlfabetizacao;
    }

    @Override
    public boolean verificarIdadeAluno(int idade) {
        return idade == 6;
    }

    public String getAulasAlfabetizacao() { return aulasAlfabetizacao; }
    public void setAulasAlfabetizacao(String aulasAlfabetizacao) { this.aulasAlfabetizacao = aulasAlfabetizacao; }

    @Override
    public String toString() {
        return super.toString() + ", Aulas de Alfabetização: " + aulasAlfabetizacao;
    }
}

class Matricula {
    private int numeroMatricula;
    private LocalDate data;
    private SituacaoMatricula situacao;
    private String observacoes;
    private String endereco;
    private Aluno aluno;
    private List<Responsavel> responsaveis;
    private Funcionario funcionario;
    private Turma turma;
    private boolean declaracaoOrientacoes;
    private LocalDate dataDeclaracao;

    public Matricula(int numeroMatricula, LocalDate data, SituacaoMatricula situacao,
                     String observacoes, String endereco, Aluno aluno,
                     List<Responsavel> responsaveis, Funcionario funcionario, Turma turma) {
        this.numeroMatricula = numeroMatricula;
        this.data = data;
        this.situacao = situacao;
        this.observacoes = observacoes;
        this.endereco = endereco;
        this.aluno = aluno;
        this.responsaveis = responsaveis;
        this.funcionario = funcionario;
        this.turma = turma;
        this.declaracaoOrientacoes = false;
    }

    public void marcarOrientacoesRecebidas() {
        this.declaracaoOrientacoes = true;
        this.dataDeclaracao = LocalDate.now();
    }

    // GETTERS
    public int getNumeroMatricula() { return numeroMatricula; }
    public LocalDate getData() { return data; }
    public SituacaoMatricula getSituacao() { return situacao; }
    public String getObservacoes() { return observacoes; }
    public String getEndereco() { return endereco; }
    public Aluno getAluno() { return aluno; }
    public List<Responsavel> getResponsaveis() { return responsaveis; }
    public Funcionario getFuncionario() { return funcionario; }
    public Turma getTurma() { return turma; }
    public boolean isDeclaracaoOrientacoes() { return declaracaoOrientacoes; }
    public LocalDate getDataDeclaracao() { return dataDeclaracao; }

    // SETTERS
    public void setNumeroMatricula(int numeroMatricula) {
        this.numeroMatricula = numeroMatricula;
    }
    public void setSituacao(SituacaoMatricula situacao) {
        this.situacao = situacao;
    }
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
    public void setTurma(Turma turma) {
        this.turma = turma;
    }
    public void setDeclaracaoOrientacoes(boolean declaracaoOrientacoes) {
        this.declaracaoOrientacoes = declaracaoOrientacoes;
    }
    public void setDataDeclaracao(LocalDate dataDeclaracao) {
        this.dataDeclaracao = dataDeclaracao;
    }

    @Override
    public String toString() {
        return "Número: " + numeroMatricula + ", Data: " + data + ", Situação: " + situacao +
                ", Aluno: " + aluno.getNome() + ", Turma: " + (turma != null ? turma.getNome() : "Não definida") +
                ", Responsáveis: " + responsaveis.size() + ", Funcionário: " + funcionario.getNome();
    }
}

class SearchableComboBox<T> extends JComboBox<T> {
    private DefaultComboBoxModel<T> model;
    private List<T> allItems;
    private boolean isSettingItems = false;

    public SearchableComboBox() {
        super();
        this.model = new DefaultComboBoxModel<>();
        this.allItems = new ArrayList<>();
        setModel(model);
        setEditable(true);

        JTextField textField = (JTextField) getEditor().getEditorComponent();
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String text = textField.getText();
                filterItems(text);
            }
        });
    }

    public void setItems(List<T> items) {
        this.allItems = new ArrayList<>(items);
        isSettingItems = true;
        updateModel(allItems);
        isSettingItems = false;
    }

    private void filterItems(String filter) {
        List<T> filtered = new ArrayList<>();
        for (T item : allItems) {
            if (item.toString().toLowerCase().contains(filter.toLowerCase())) {
                filtered.add(item);
            }
        }
        updateModel(filtered);
    }

    private void updateModel(List<T> items) {
        model.removeAllElements();
        for (T item : items) {
            model.addElement(item);
        }
        setSelectedIndex(-1);
        if (!isSettingItems && isShowing()) {
            showPopup();
        }
    }
}

// Sistema principal integrado
public class SistemaCrecheGUICompleto {
    private List<Aluno> alunos = new ArrayList<>();
    private List<Funcionario> funcionarios = new ArrayList<>();
    private List<Responsavel> responsaveis = new ArrayList<>();
    private List<Matricula> matriculas = new ArrayList<>();
    private List<Turma> turmas = new ArrayList<>();

    private ConnectionFactory connectionFactory = new ConnectionFactory();
    private AlunoDAO alunoDAO;
    private FuncionarioDAO funcionarioDAO;
    private ResponsavelDAO responsavelDAO;
    private TurmaDAO turmaDAO;
    private MatriculaDAO matriculaDAO;
    private IrmaosDAO irmaosDAO;

    private int nextAlunoId = 1;
    private int nextFuncionarioId = 1;
    private int nextResponsavelId = 1;
    private int nextMatriculaId = 1;
    private int nextTurmaId = 1;

    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JTable tableAlunos, tableFuncionarios, tableResponsaveis, tableMatriculas, tableTurmas, tablePreMatriculas;
    private TableRowSorter<DefaultTableModel> sorterAlunos, sorterFuncionarios, sorterResponsaveis, sorterMatriculas, sorterTurmas, sorterPreMatriculas;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SistemaCrecheGUICompleto().inicializar();
        });
    }

    public void inicializar() {
        try {
            // Inicializar DAOs
            Connection conn = connectionFactory.recuperarConexao();
            alunoDAO = new AlunoDAO(conn);
            funcionarioDAO = new FuncionarioDAO(conn);
            responsavelDAO = new ResponsavelDAO(conn);
            turmaDAO = new TurmaDAO(conn);
            matriculaDAO = new MatriculaDAO(conn);
            irmaosDAO = new IrmaosDAO(conn);

            // Carregar dados do banco
            carregarDadosDoBanco();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao conectar com banco de dados: " + e.getMessage() +
                            "\nSistema funcionará com dados em memória.", "Erro de Conexão",
                    JOptionPane.WARNING_MESSAGE);
        }

        cadastrarDadosIniciais();
        criarInterface();
    }

    private void carregarDadosDoBanco() throws SQLException {
        alunos = alunoDAO.listarTodos();
        funcionarios = funcionarioDAO.listarTodos();
        responsaveis = responsavelDAO.listarTodos();
        turmas = turmaDAO.listarTodos();
        matriculas = matriculaDAO.listarTodos();

        // Atualizar IDs baseado no banco
        if (!alunos.isEmpty()) {
            nextAlunoId = alunos.stream().mapToInt(Aluno::getId).max().getAsInt() + 1;
        }
        if (!funcionarios.isEmpty()) {
            nextFuncionarioId = funcionarios.stream().mapToInt(Funcionario::getId).max().getAsInt() + 1;
        }
        if (!responsaveis.isEmpty()) {
            nextResponsavelId = responsaveis.stream().mapToInt(Responsavel::getId).max().getAsInt() + 1;
        }
        if (!turmas.isEmpty()) {
            nextTurmaId = turmas.stream().mapToInt(Turma::getId).max().getAsInt() + 1;
        }
        if (!matriculas.isEmpty()) {
            nextMatriculaId = matriculas.stream().mapToInt(Matricula::getNumeroMatricula).max().getAsInt() + 1;
        }
    }

    private void criarInterface() {
        frame = new JFrame("Sistema de Gerenciamento da Creche Estrela do Oriente - Com Banco de Dados");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Alunos", criarPainelAlunos());
        tabbedPane.addTab("Funcionários", criarPainelFuncionarios());
        tabbedPane.addTab("Responsáveis", criarPainelResponsaveis());
        tabbedPane.addTab("Turmas", criarPainelTurmas());
        tabbedPane.addTab("Pré-Matrículas", criarPainelPreMatriculas());
        tabbedPane.addTab("Matrículas", criarPainelMatriculas());
        tabbedPane.addTab("Relatórios", criarPainelRelatorios());

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private JPanel criarPainelAlunos() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel panelBotoes = new JPanel(new FlowLayout());
        JButton btnCadastrar = new JButton("Cadastrar Aluno");
        JButton btnAtualizar = new JButton("Atualizar Lista");
        JButton btnDetalhes = new JButton("Ver Detalhes");
        JButton btnEditar = new JButton("Editar Aluno");
        JButton btnMaisInfo = new JButton("Mais Informações");
        JButton btnIrmaos = new JButton("Associar Irmãos");

        panelBotoes.add(btnCadastrar);
        panelBotoes.add(btnAtualizar);
        panelBotoes.add(btnDetalhes);
        panelBotoes.add(btnEditar);
        panelBotoes.add(btnMaisInfo);
        panelBotoes.add(btnIrmaos);

        String[] colunas = {"ID", "Nome", "Data Nasc.", "Idade", "Sexo", "Turma", "Responsável"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        tableAlunos = new JTable(model);
        sorterAlunos = new TableRowSorter<>(model);
        tableAlunos.setRowSorter(sorterAlunos);
        atualizarTabelaAlunos();

        JScrollPane scrollPane = new JScrollPane(tableAlunos);

        JPanel panelFiltro = new JPanel(new FlowLayout());
        panelFiltro.add(new JLabel("Filtrar:"));
        JTextField txtFiltro = new JTextField(20);
        panelFiltro.add(txtFiltro);

        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtFiltro.getText();
                if (text.trim().length() == 0) {
                    sorterAlunos.setRowFilter(null);
                } else {
                    sorterAlunos.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        panel.add(panelFiltro, BorderLayout.NORTH);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        btnCadastrar.addActionListener(e -> cadastrarAluno());
        btnAtualizar.addActionListener(e -> atualizarTabelaAlunos());
        btnDetalhes.addActionListener(e -> mostrarDetalhesAluno());
        btnEditar.addActionListener(e -> editarAluno());
        btnMaisInfo.addActionListener(e -> mostrarMaisInformacoesAluno());
        btnIrmaos.addActionListener(e -> adicionarIrmaos());

        return panel;
    }

    private JPanel criarPainelFuncionarios() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel panelBotoes = new JPanel(new FlowLayout());
        JButton btnCadastrar = new JButton("Cadastrar Funcionário");
        JButton btnAtualizar = new JButton("Atualizar Lista");
        JButton btnEditar = new JButton("Editar Funcionário");

        panelBotoes.add(btnCadastrar);
        panelBotoes.add(btnAtualizar);
        panelBotoes.add(btnEditar);

        String[] colunas = {"ID", "Nome", "Idade", "CPF", "Cargo", "Vínculo", "Tipo", "Detalhes"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        tableFuncionarios = new JTable(model);
        sorterFuncionarios = new TableRowSorter<>(model);
        tableFuncionarios.setRowSorter(sorterFuncionarios);
        atualizarTabelaFuncionarios();

        JScrollPane scrollPane = new JScrollPane(tableFuncionarios);

        JPanel panelFiltro = new JPanel(new FlowLayout());
        panelFiltro.add(new JLabel("Filtrar:"));
        JTextField txtFiltro = new JTextField(20);
        panelFiltro.add(txtFiltro);

        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtFiltro.getText();
                if (text.trim().length() == 0) {
                    sorterFuncionarios.setRowFilter(null);
                } else {
                    sorterFuncionarios.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        panel.add(panelFiltro, BorderLayout.NORTH);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        btnCadastrar.addActionListener(e -> cadastrarFuncionario());
        btnAtualizar.addActionListener(e -> atualizarTabelaFuncionarios());
        btnEditar.addActionListener(e -> editarFuncionario());

        return panel;
    }

    private JPanel criarPainelResponsaveis() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel panelBotoes = new JPanel(new FlowLayout());
        JButton btnCadastrar = new JButton("Cadastrar Responsável");
        JButton btnAtualizar = new JButton("Atualizar Lista");
        JButton btnEditar = new JButton("Editar Responsável");

        panelBotoes.add(btnCadastrar);
        panelBotoes.add(btnAtualizar);
        panelBotoes.add(btnEditar);

        String[] colunas = {"ID", "Nome", "Idade", "CPF", "Parentesco", "Telefone", "Voluntariado"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        tableResponsaveis = new JTable(model);
        sorterResponsaveis = new TableRowSorter<>(model);
        tableResponsaveis.setRowSorter(sorterResponsaveis);
        atualizarTabelaResponsaveis();

        JScrollPane scrollPane = new JScrollPane(tableResponsaveis);

        JPanel panelFiltro = new JPanel(new FlowLayout());
        panelFiltro.add(new JLabel("Filtrar:"));
        JTextField txtFiltro = new JTextField(20);
        panelFiltro.add(txtFiltro);

        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtFiltro.getText();
                if (text.trim().length() == 0) {
                    sorterResponsaveis.setRowFilter(null);
                } else {
                    sorterResponsaveis.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        panel.add(panelFiltro, BorderLayout.NORTH);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        btnCadastrar.addActionListener(e -> cadastrarResponsavel());
        btnAtualizar.addActionListener(e -> atualizarTabelaResponsaveis());
        btnEditar.addActionListener(e -> editarResponsavel());

        return panel;
    }

    private JPanel criarPainelTurmas() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel panelBotoes = new JPanel(new FlowLayout());
        JButton btnCadastrar = new JButton("Cadastrar Turma");
        JButton btnAtualizar = new JButton("Atualizar Lista");
        JButton btnDetalhes = new JButton("Ver Detalhes");
        JButton btnEditar = new JButton("Editar Turma");

        panelBotoes.add(btnCadastrar);
        panelBotoes.add(btnAtualizar);
        panelBotoes.add(btnDetalhes);
        panelBotoes.add(btnEditar);

        String[] colunas = {"ID", "Nome", "Faixa Etária", "Turno", "Professor", "Alunos", "Limite"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        tableTurmas = new JTable(model);
        sorterTurmas = new TableRowSorter<>(model);
        tableTurmas.setRowSorter(sorterTurmas);
        atualizarTabelaTurmas();

        JScrollPane scrollPane = new JScrollPane(tableTurmas);

        JPanel panelFiltro = new JPanel(new FlowLayout());
        panelFiltro.add(new JLabel("Filtrar:"));
        JTextField txtFiltro = new JTextField(20);
        panelFiltro.add(txtFiltro);

        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtFiltro.getText();
                if (text.trim().length() == 0) {
                    sorterTurmas.setRowFilter(null);
                } else {
                    sorterTurmas.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        panel.add(panelFiltro, BorderLayout.NORTH);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        btnCadastrar.addActionListener(e -> cadastrarTurma());
        btnAtualizar.addActionListener(e -> atualizarTabelaTurmas());
        btnDetalhes.addActionListener(e -> mostrarDetalhesTurma());
        btnEditar.addActionListener(e -> editarTurma());

        return panel;
    }

    private JPanel criarPainelPreMatriculas() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel panelBotoes = new JPanel(new FlowLayout());
        JButton btnCadastrar = new JButton("Nova Pré-Matrícula");
        JButton btnAtualizar = new JButton("Atualizar Lista");
        JButton btnAtivar = new JButton("Ativar Matrícula");

        panelBotoes.add(btnCadastrar);
        panelBotoes.add(btnAtualizar);
        panelBotoes.add(btnAtivar);

        String[] colunas = {"Número", "Data", "Situação", "Aluno", "Funcionário", "Responsáveis"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        tablePreMatriculas = new JTable(model);
        sorterPreMatriculas = new TableRowSorter<>(model);
        tablePreMatriculas.setRowSorter(sorterPreMatriculas);
        atualizarTabelaPreMatriculas();

        JScrollPane scrollPane = new JScrollPane(tablePreMatriculas);

        JPanel panelFiltro = new JPanel(new FlowLayout());
        panelFiltro.add(new JLabel("Filtrar:"));
        JTextField txtFiltro = new JTextField(20);
        panelFiltro.add(txtFiltro);

        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtFiltro.getText();
                if (text.trim().length() == 0) {
                    sorterPreMatriculas.setRowFilter(null);
                } else {
                    sorterPreMatriculas.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        panel.add(panelFiltro, BorderLayout.NORTH);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        btnCadastrar.addActionListener(e -> cadastrarPreMatricula());
        btnAtualizar.addActionListener(e -> atualizarTabelaPreMatriculas());
        btnAtivar.addActionListener(e -> ativarMatricula());

        return panel;
    }

    private JPanel criarPainelMatriculas() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel panelBotoes = new JPanel(new FlowLayout());
        JButton btnAtualizar = new JButton("Atualizar Lista");
        JButton btnEditar = new JButton("Editar Matrícula");
        JButton btnCompletarDados = new JButton("Completar Dados");
        JButton btnVerDetalhes = new JButton("Ver Detalhes Completos");
        JButton btnEditarSocioeconomico = new JButton("Editar Dados Socioeconômicos");

        panelBotoes.add(btnAtualizar);
        panelBotoes.add(btnEditar);
        panelBotoes.add(btnCompletarDados);
        panelBotoes.add(btnVerDetalhes);
        panelBotoes.add(btnEditarSocioeconomico);

        String[] colunas = {"Número", "Data", "Situação", "Aluno", "Turma", "Funcionário", "Orientação"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        tableMatriculas = new JTable(model);
        sorterMatriculas = new TableRowSorter<>(model);
        tableMatriculas.setRowSorter(sorterMatriculas);
        atualizarTabelaMatriculas();

        JScrollPane scrollPane = new JScrollPane(tableMatriculas);

        JPanel panelFiltro = new JPanel(new FlowLayout());
        panelFiltro.add(new JLabel("Filtrar:"));
        JTextField txtFiltro = new JTextField(20);
        panelFiltro.add(txtFiltro);

        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtFiltro.getText();
                if (text.trim().length() == 0) {
                    sorterMatriculas.setRowFilter(null);
                } else {
                    sorterMatriculas.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        panel.add(panelFiltro, BorderLayout.NORTH);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        btnAtualizar.addActionListener(e -> atualizarTabelaMatriculas());
        btnEditar.addActionListener(e -> editarMatricula());
        btnCompletarDados.addActionListener(e -> completarDadosSocioeconomicos());
        btnVerDetalhes.addActionListener(e -> mostrarDetalhesCompletosMatricula());
        btnEditarSocioeconomico.addActionListener(e -> editarDadosSocioeconomicos());

        return panel;
    }

    private JPanel criarPainelRelatorios() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Gerar Relatórios - Creche Estrela do Oriente", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new GridLayout(6, 1, 10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JButton btnRelatorioAlunos = new JButton("Relatório de Alunos");
        JButton btnRelatorioFuncionarios = new JButton("Relatório de Funcionários");
        JButton btnRelatorioTurmas = new JButton("Relatório de Turmas");
        JButton btnRelatorioMatriculas = new JButton("Relatório de Matrículas");
        JButton btnRelatorioSocioeconomico = new JButton("Relatório Socioeconômico");
        JButton btnRelatorioCompleto = new JButton("Relatório Completo");

        btnRelatorioAlunos.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRelatorioFuncionarios.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRelatorioTurmas.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRelatorioMatriculas.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRelatorioSocioeconomico.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRelatorioCompleto.setFont(new Font("Arial", Font.BOLD, 14));

        panelCentral.add(btnRelatorioAlunos);
        panelCentral.add(btnRelatorioFuncionarios);
        panelCentral.add(btnRelatorioTurmas);
        panelCentral.add(btnRelatorioMatriculas);
        panelCentral.add(btnRelatorioSocioeconomico);
        panelCentral.add(btnRelatorioCompleto);

        panel.add(panelCentral, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel();
        JLabel lblInfo = new JLabel("Os relatórios serão salvos na pasta 'relatorios' como arquivos TXT");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        panelInferior.add(lblInfo);
        panel.add(panelInferior, BorderLayout.SOUTH);

        btnRelatorioAlunos.addActionListener(e -> gerarRelatorioAlunos());
        btnRelatorioFuncionarios.addActionListener(e -> gerarRelatorioFuncionarios());
        btnRelatorioTurmas.addActionListener(e -> gerarRelatorioTurmas());
        btnRelatorioMatriculas.addActionListener(e -> gerarRelatorioMatriculas());
        btnRelatorioSocioeconomico.addActionListener(e -> gerarRelatorioSocioeconomico());
        btnRelatorioCompleto.addActionListener(e -> gerarRelatorioCompleto());

        return panel;
    }

    // Métodos de cadastro com integração ao banco de dados
    private void cadastrarAluno() {
        JDialog dialog = new JDialog(frame, "Cadastrar Aluno", true);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(frame);

        JTextField txtNome = new JTextField();
        JTextField txtDataNascimento = new JTextField();
        JTextField txtCpf = new JTextField();
        JComboBox<String> cmbSexo = new JComboBox<>(new String[]{"Masculino", "Feminino"});
        JTextField txtCorRaca = new JTextField();
        JTextField txtProblemasSaude = new JTextField();

        dialog.add(new JLabel("Nome:*"));
        dialog.add(txtNome);
        dialog.add(new JLabel("Data Nasc. (DD-MM-AAAA):*"));
        dialog.add(txtDataNascimento);
        dialog.add(new JLabel("CPF:*"));
        dialog.add(txtCpf);
        dialog.add(new JLabel("Sexo:*"));
        dialog.add(cmbSexo);
        dialog.add(new JLabel("Cor/Raça:"));
        dialog.add(txtCorRaca);
        dialog.add(new JLabel("Problemas Saúde:"));
        dialog.add(txtProblemasSaude);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");

        btnSalvar.addActionListener(e -> {
            try {
                String nome = txtNome.getText();
                LocalDate dataNascimento = parseData(txtDataNascimento.getText());
                String cpf = txtCpf.getText();
                String sexo = (String) cmbSexo.getSelectedItem();
                String corRaca = txtCorRaca.getText();
                String problemasSaude = txtProblemasSaude.getText();

                if (nome.isEmpty() || cpf.isEmpty() || dataNascimento == null) {
                    JOptionPane.showMessageDialog(dialog, "Preencha os campos obrigatórios!");
                    return;
                }

                Aluno aluno = new Aluno(0, nome, dataNascimento, cpf, sexo, corRaca, problemasSaude);

                try {
                    alunoDAO.cadastrar(aluno);
                    alunos = alunoDAO.listarTodos(); // Recarregar do banco
                    atualizarTabelaAlunos();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(frame, "Aluno cadastrado com sucesso!");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Erro ao salvar no banco: " + ex.getMessage());
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erro no formato da data! Use DD-MM-AAAA");
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        JPanel panelBotoes = new JPanel();
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnCancelar);

        dialog.add(new JLabel());
        dialog.add(panelBotoes);
        dialog.setVisible(true);
    }

    private void cadastrarFuncionario() {
        JDialog dialog = new JDialog(frame, "Cadastrar Funcionário", true);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(frame);

        JTextField txtNome = new JTextField();
        JTextField txtIdade = new JTextField();
        JTextField txtCpf = new JTextField();
        JTextField txtVinculo = new JTextField();

        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Professor", "Assistente", "Coordenador"});
        JComboBox<String> cmbVoluntario = new JComboBox<>(new String[]{"Sim", "Não"});
        JTextField txtDetalhe = new JTextField();
        JLabel lblDetalhe = new JLabel("Detalhe:");

        dialog.add(new JLabel("Tipo:*"));
        dialog.add(cmbTipo);
        dialog.add(new JLabel("Nome:*"));
        dialog.add(txtNome);
        dialog.add(new JLabel("Idade:*"));
        dialog.add(txtIdade);
        dialog.add(new JLabel("CPF:*"));
        dialog.add(txtCpf);
        dialog.add(new JLabel("Vínculo:*"));
        dialog.add(txtVinculo);
        dialog.add(new JLabel("Voluntário:*"));
        dialog.add(cmbVoluntario);
        dialog.add(lblDetalhe);
        dialog.add(txtDetalhe);

        cmbTipo.addActionListener(e -> {
            String tipo = (String) cmbTipo.getSelectedItem();
            switch (tipo) {
                case "Professor":
                    lblDetalhe.setText("Turno:*");
                    txtDetalhe.setText("Manhã");
                    break;
                case "Assistente":
                    lblDetalhe.setText("Faixa etária:*");
                    txtDetalhe.setText("2-3 anos");
                    break;
                case "Coordenador":
                    lblDetalhe.setText("Setor:*");
                    txtDetalhe.setText("Pedagógico");
                    break;
            }
        });

        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");

        btnSalvar.addActionListener(e -> {
            try {
                String tipo = (String) cmbTipo.getSelectedItem();
                String nome = txtNome.getText();
                int idade = Integer.parseInt(txtIdade.getText());
                String cpf = txtCpf.getText();
                String vinculo = txtVinculo.getText();
                boolean voluntario = "Sim".equals(cmbVoluntario.getSelectedItem());
                String detalhe = txtDetalhe.getText();

                if (nome.isEmpty() || cpf.isEmpty() || vinculo.isEmpty() || detalhe.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Preencha todos os campos!");
                    return;
                }

                Funcionario funcionario = null;
                switch (tipo) {
                    case "Professor":
                        funcionario = new Professor(0, nome, idade, cpf, vinculo, detalhe, voluntario);
                        break;
                    case "Assistente":
                        funcionario = new Assistente(0, nome, idade, cpf, vinculo, detalhe, voluntario);
                        break;
                    case "Coordenador":
                        funcionario = new Coordenador(0, nome, idade, cpf, vinculo, detalhe, voluntario);
                        break;
                }

                try {
                    funcionarioDAO.cadastrar(funcionario);
                    funcionarios = funcionarioDAO.listarTodos();
                    atualizarTabelaFuncionarios();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(frame, "Funcionário cadastrado com sucesso!");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Erro ao salvar no banco: " + ex.getMessage());
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Idade deve ser um número válido!");
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        JPanel panelBotoes = new JPanel();
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnCancelar);

        dialog.add(new JLabel());
        dialog.add(panelBotoes);
        dialog.setVisible(true);
    }

    private void cadastrarResponsavel() {
        JDialog dialog = new JDialog(frame, "Cadastrar Responsável", true);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(frame);

        JTextField txtNome = new JTextField();
        JTextField txtIdade = new JTextField();
        JTextField txtCpf = new JTextField();
        JTextField txtParentesco = new JTextField();
        JTextField txtTelefone = new JTextField();
        JTextField txtCelular = new JTextField();

        dialog.add(new JLabel("Nome:*"));
        dialog.add(txtNome);
        dialog.add(new JLabel("Idade:*"));
        dialog.add(txtIdade);
        dialog.add(new JLabel("CPF:*"));
        dialog.add(txtCpf);
        dialog.add(new JLabel("Parentesco:*"));
        dialog.add(txtParentesco);
        dialog.add(new JLabel("Telefone:*"));
        dialog.add(txtTelefone);
        dialog.add(new JLabel("Celular/WhatsApp:*"));
        dialog.add(txtCelular);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");

        btnSalvar.addActionListener(e -> {
            try {
                String nome = txtNome.getText();
                int idade = Integer.parseInt(txtIdade.getText());
                String cpf = txtCpf.getText();
                String parentesco = txtParentesco.getText();
                String telefone = txtTelefone.getText();
                String celular = txtCelular.getText();

                if (nome.isEmpty() || cpf.isEmpty() || parentesco.isEmpty() || telefone.isEmpty() || celular.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Preencha todos os campos!");
                    return;
                }

                Responsavel responsavel = new Responsavel(0, nome, idade, cpf, parentesco, telefone, celular);

                try {
                    responsavelDAO.cadastrar(responsavel);
                    responsaveis = responsavelDAO.listarTodos();
                    atualizarTabelaResponsaveis();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(frame, "Responsável cadastrado com sucesso!");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Erro ao salvar no banco: " + ex.getMessage());
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Idade deve ser um número válido!");
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        JPanel panelBotoes = new JPanel();
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnCancelar);

        dialog.add(new JLabel());
        dialog.add(panelBotoes);
        dialog.setVisible(true);
    }

    private void cadastrarTurma() {
        JDialog dialog = new JDialog(frame, "Cadastrar Turma", true);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(frame);

        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Creche (2-3 anos)", "Infantil (4-5 years)", "Pré (6 anos)"});
        JTextField txtNome = new JTextField();
        JTextField txtTurno = new JTextField();
        JComboBox<Professor> cmbProfessor = new JComboBox<>();

        try {
            List<Funcionario> professores = funcionarioDAO.listarPorCargo("Professor");
            for (Funcionario func : professores) {
                if (func instanceof Professor) {
                    cmbProfessor.addItem((Professor) func);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "Erro ao carregar professores: " + ex.getMessage());
        }

        dialog.add(new JLabel("Tipo:*"));
        dialog.add(cmbTipo);
        dialog.add(new JLabel("Nome:*"));
        dialog.add(txtNome);
        dialog.add(new JLabel("Turno:*"));
        dialog.add(txtTurno);
        dialog.add(new JLabel("Professor:"));
        dialog.add(cmbProfessor);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");

        btnSalvar.addActionListener(e -> {
            String tipo = (String) cmbTipo.getSelectedItem();
            String nome = txtNome.getText();
            String turno = txtTurno.getText();
            Professor professor = (Professor) cmbProfessor.getSelectedItem();

            if (nome.isEmpty() || turno.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Preencha todos os campos!");
                return;
            }

            Turma turma = null;
            switch (tipo) {
                case "Creche (2-3 anos)":
                    turma = new TurmaCreche(0, nome, turno);
                    break;
                case "Infantil (4-5 years)":
                    turma = new TurmaInfantil(0, nome, turno);
                    break;
                case "Pré (6 anos)":
                    turma = new TurmaPre(0, nome, turno, "Diárias");
                    break;
            }

            if (professor != null) {
                turma.setProfessor(professor);
            }

            try {
                turmaDAO.cadastrar(turma);
                turmas = turmaDAO.listarTodos();
                atualizarTabelaTurmas();
                dialog.dispose();
                JOptionPane.showMessageDialog(frame, "Turma cadastrada com sucesso!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao salvar no banco: " + ex.getMessage());
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        JPanel panelBotoes = new JPanel();
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnCancelar);

        dialog.add(new JLabel());
        dialog.add(panelBotoes);
        dialog.setVisible(true);
    }

    private void cadastrarPreMatricula() {
        if (alunos.isEmpty() || responsaveis.isEmpty() || funcionarios.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "É necessário ter pelo menos:\n- 1 Aluno\n- 1 Responsável\n- 1 Funcionário\n\nCadastre esses dados primeiro.");
            return;
        }

        JDialog dialog = new JDialog(frame, "Nova Pré-Matrícula", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(frame);

        JPanel panelPrincipal = new JPanel(new GridLayout(0, 2, 10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        SearchableComboBox<Aluno> cmbAluno = new SearchableComboBox<>();
        SearchableComboBox<Funcionario> cmbFuncionario = new SearchableComboBox<>();
        JComboBox<SituacaoMatricula> cmbSituacao = new JComboBox<>(new SituacaoMatricula[]{SituacaoMatricula.PRE_MATRICULA});
        JList<Responsavel> listResponsaveis = new JList<>();
        DefaultListModel<Responsavel> listModel = new DefaultListModel<>();
        JTextField txtObservacoes = new JTextField();
        JTextField txtEndereco = new JTextField();
        JCheckBox chkOrientacoes = new JCheckBox("Declaro ter recebido as orientações");

        SwingUtilities.invokeLater(() -> {
            cmbAluno.setItems(alunos);
            List<Funcionario> todosFuncionarios = new ArrayList<>(funcionarios);
            cmbFuncionario.setItems(todosFuncionarios);
        });

        for (Responsavel resp : responsaveis) {
            listModel.addElement(resp);
        }
        listResponsaveis.setModel(listModel);
        listResponsaveis.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        panelPrincipal.add(new JLabel("Aluno:*"));
        panelPrincipal.add(cmbAluno);
        panelPrincipal.add(new JLabel("Funcionário Responsável:*"));
        panelPrincipal.add(cmbFuncionario);
        panelPrincipal.add(new JLabel("Situação:*"));
        panelPrincipal.add(cmbSituacao);
        panelPrincipal.add(new JLabel("Responsáveis:*"));
        panelPrincipal.add(new JScrollPane(listResponsaveis));
        panelPrincipal.add(new JLabel("Observações:"));
        panelPrincipal.add(txtObservacoes);
        panelPrincipal.add(new JLabel("Endereço:*"));
        panelPrincipal.add(txtEndereco);
        panelPrincipal.add(new JLabel());
        panelPrincipal.add(chkOrientacoes);

        JButton btnSalvar = new JButton("Salvar Pré-Matrícula");
        JButton btnCancelar = new JButton("Cancelar");

        btnSalvar.addActionListener(e -> {
            Aluno alunoSelecionado = (Aluno) cmbAluno.getSelectedItem();
            Funcionario funcionarioSelecionado = (Funcionario) cmbFuncionario.getSelectedItem();
            List<Responsavel> responsaveisSelecionados = listResponsaveis.getSelectedValuesList();
            String endereco = txtEndereco.getText().trim();

            if (alunoSelecionado == null || funcionarioSelecionado == null) {
                JOptionPane.showMessageDialog(dialog, "Selecione aluno e funcionário!");
                return;
            }

            if (responsaveisSelecionados.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Selecione pelo menos um responsável!");
                return;
            }

            if (endereco.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Preencha o endereço!");
                return;
            }

            if (!chkOrientacoes.isSelected()) {
                JOptionPane.showMessageDialog(dialog, "É necessário confirmar o recebimento das orientações!");
                return;
            }

            boolean jaTemPreMatricula = matriculas.stream()
                    .anyMatch(mat -> mat.getAluno().equals(alunoSelecionado) &&
                            mat.getSituacao() == SituacaoMatricula.PRE_MATRICULA);

            if (jaTemPreMatricula) {
                JOptionPane.showMessageDialog(dialog,
                        "Este aluno já possui uma pré-matrícula ativa!\n" +
                                "Aluno: " + alunoSelecionado.getNome());
                return;
            }

            int confirmacao = JOptionPane.showConfirmDialog(dialog,
                    "Confirmar pré-matrícula?\n\n" +
                            "Aluno: " + alunoSelecionado.getNome() + "\n" +
                            "Funcionário: " + funcionarioSelecionado.getNome() + "\n" +
                            "Responsáveis: " + responsaveisSelecionados.size() + "\n" +
                            "Situação: " + cmbSituacao.getSelectedItem(),
                    "Confirmar Pré-Matrícula",
                    JOptionPane.YES_NO_OPTION);

            if (confirmacao == JOptionPane.YES_OPTION) {
                try {
                    Matricula novaMatricula = new Matricula(
                            nextMatriculaId++,
                            LocalDate.now(),
                            SituacaoMatricula.PRE_MATRICULA,
                            txtObservacoes.getText().trim(),
                            endereco,
                            alunoSelecionado,
                            responsaveisSelecionados,
                            funcionarioSelecionado,
                            null
                    );

                    novaMatricula.marcarOrientacoesRecebidas();

                    matriculaDAO.cadastrar(novaMatricula);

                    // Adicionar responsáveis à matrícula no banco
                    for (Responsavel resp : responsaveisSelecionados) {
                        matriculaDAO.adicionarResponsavelMatricula(novaMatricula.getNumeroMatricula(), resp.getId());
                    }

                    matriculas = matriculaDAO.listarTodos();
                    atualizarTabelaPreMatriculas();

                    dialog.dispose();

                    StringBuilder resumo = new StringBuilder();
                    resumo.append("✅ PRÉ-MATRÍCULA REALIZADA COM SUCESSO!\n\n");
                    resumo.append("Número: ").append(novaMatricula.getNumeroMatricula()).append("\n");
                    resumo.append("Aluno: ").append(alunoSelecionado.getNome()).append("\n");
                    resumo.append("Situação: ").append(novaMatricula.getSituacao()).append("\n");
                    resumo.append("Responsáveis: ").append(responsaveisSelecionados.size()).append("\n");

                    for (Responsavel resp : responsaveisSelecionados) {
                        resumo.append("  • ").append(resp.getNome()).append(" (").append(resp.getParentesco()).append(")\n");
                    }

                    resumo.append("\n⚠️  ATENÇÃO: Esta é uma PRÉ-MATRÍCULA.\n");
                    resumo.append("Para completar os dados e ativar a matrícula:\n");
                    resumo.append("1. Selecione na aba 'Pré-Matrículas'\n");
                    resumo.append("2. Clique em 'Ativar Matrícula'\n");
                    resumo.append("3. Preencha os dados socioeconômicos completos");

                    JOptionPane.showMessageDialog(frame, resumo.toString(), "Pré-Matrícula Concluída", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Erro ao criar pré-matrícula: " + ex.getMessage());
                }
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        JPanel panelBotoes = new JPanel();
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnCancelar);

        dialog.add(panelPrincipal, BorderLayout.CENTER);
        dialog.add(panelBotoes, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // Método para associar irmãos (do primeiro código adaptado)
    private void adicionarIrmaos() {
        try {
            alunos = alunoDAO.listarTodos();

            if (alunos.size() < 2) {
                JOptionPane.showMessageDialog(frame, "É necessário ter pelo menos dois alunos cadastrados!");
                return;
            }

            JDialog dialog = new JDialog(frame, "Associar Irmãos", true);
            dialog.setLayout(new GridLayout(0, 2, 10, 10));
            dialog.setSize(400, 200);
            dialog.setLocationRelativeTo(frame);

            JComboBox<Aluno> cmbAluno1 = new JComboBox<>();
            JComboBox<Aluno> cmbAluno2 = new JComboBox<>();

            for (Aluno aluno : alunos) {
                cmbAluno1.addItem(aluno);
                cmbAluno2.addItem(aluno);
            }

            dialog.add(new JLabel("Primeiro Aluno:"));
            dialog.add(cmbAluno1);
            dialog.add(new JLabel("Segundo Aluno:"));
            dialog.add(cmbAluno2);

            JButton btnAssociar = new JButton("Associar como Irmãos");
            JButton btnCancelar = new JButton("Cancelar");

            btnAssociar.addActionListener(e -> {
                Aluno a1 = (Aluno) cmbAluno1.getSelectedItem();
                Aluno a2 = (Aluno) cmbAluno2.getSelectedItem();

                if (a1.equals(a2)) {
                    JOptionPane.showMessageDialog(dialog, "Um aluno não pode ser irmão de si mesmo!");
                    return;
                }

                try {
                    if (!irmaosDAO.saoIrmaos(a1.getId(), a2.getId())) {
                        irmaosDAO.adicionarIrmaos(a1.getId(), a2.getId());
                        JOptionPane.showMessageDialog(dialog,
                                "Irmãos associados com sucesso: " + a1.getNome() + " e " + a2.getNome());
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Estes alunos já são irmãos!");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Erro ao associar irmãos: " + ex.getMessage());
                }
            });

            btnCancelar.addActionListener(e -> dialog.dispose());

            JPanel panelBotoes = new JPanel();
            panelBotoes.add(btnAssociar);
            panelBotoes.add(btnCancelar);

            dialog.add(new JLabel());
            dialog.add(panelBotoes);
            dialog.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Erro ao carregar alunos: " + e.getMessage());
        }
    }

    // Métodos de atualização de tabelas
    private void atualizarTabelaAlunos() {
        DefaultTableModel model = (DefaultTableModel) tableAlunos.getModel();
        model.setRowCount(0);

        for (Aluno aluno : alunos) {
            model.addRow(new Object[]{
                    aluno.getId(),
                    aluno.getNome(),
                    formatarData(aluno.getDataNascimento()),
                    aluno.getIdade(),
                    aluno.getSexo(),
                    aluno.getTurma() != null ? aluno.getTurma().getNome() : "Sem turma",
                    aluno.getResponsavelPrioritario() != null ? aluno.getResponsavelPrioritario().getNome() : "Nenhum"
            });
        }
    }

    private void atualizarTabelaFuncionarios() {
        DefaultTableModel model = (DefaultTableModel) tableFuncionarios.getModel();
        model.setRowCount(0);

        for (Funcionario func : funcionarios) {
            String detalhes = "";
            if (func instanceof Professor) {
                detalhes = "Turno: " + ((Professor) func).getTurno();
            } else if (func instanceof Assistente) {
                detalhes = "Faixa: " + ((Assistente) func).getFaixaEtariaAtendida();
            } else if (func instanceof Coordenador) {
                detalhes = "Setor: " + ((Coordenador) func).getSetorResponsavel();
            }

            model.addRow(new Object[]{
                    func.getId(),
                    func.getNome(),
                    func.getIdade(),
                    func.getCpf(),
                    func.getCargo(),
                    func.getVinculo(),
                    func.isVoluntario() ? "Voluntário" : "Remunerado",
                    detalhes
            });
        }
    }

    private void atualizarTabelaResponsaveis() {
        DefaultTableModel model = (DefaultTableModel) tableResponsaveis.getModel();
        model.setRowCount(0);

        for (Responsavel resp : responsaveis) {
            model.addRow(new Object[]{
                    resp.getId(),
                    resp.getNome(),
                    resp.getIdade(),
                    resp.getCpf(),
                    resp.getParentesco(),
                    resp.getTelefone(),
                    resp.getDiasVoluntariado() + " dias/mês"
            });
        }
    }

    private void atualizarTabelaTurmas() {
        DefaultTableModel model = (DefaultTableModel) tableTurmas.getModel();
        model.setRowCount(0);

        for (Turma turma : turmas) {
            model.addRow(new Object[]{
                    turma.getId(),
                    turma.getNome(),
                    turma.getFaixaEtaria(),
                    turma.getTurno(),
                    turma.getProfessor() != null ? turma.getProfessor().getNome() : "Não atribuído",
                    turma.getAlunos().size(),
                    "18"
            });
        }
    }

    private void atualizarTabelaPreMatriculas() {
        DefaultTableModel model = (DefaultTableModel) tablePreMatriculas.getModel();
        model.setRowCount(0);

        for (Matricula mat : matriculas) {
            if (mat.getSituacao() == SituacaoMatricula.PRE_MATRICULA) {
                model.addRow(new Object[]{
                        mat.getNumeroMatricula(),
                        formatarData(mat.getData()),
                        mat.getSituacao(),
                        mat.getAluno().getNome(),
                        mat.getFuncionario().getNome(),
                        mat.getResponsaveis().size()
                });
            }
        }
    }

    private void atualizarTabelaMatriculas() {
        DefaultTableModel model = (DefaultTableModel) tableMatriculas.getModel();
        model.setRowCount(0);

        for (Matricula mat : matriculas) {
            if (mat.getSituacao() != SituacaoMatricula.PRE_MATRICULA) {
                model.addRow(new Object[]{
                        mat.getNumeroMatricula(),
                        formatarData(mat.getData()),
                        mat.getSituacao(),
                        mat.getAluno().getNome(),
                        mat.getTurma() != null ? mat.getTurma().getNome() : "Não definida",
                        mat.getFuncionario().getNome(),
                        mat.isDeclaracaoOrientacoes() ? "Sim" : "Não"
                });
            }
        }
    }

    // Métodos auxiliares
    private String formatarData(LocalDate data) {
        if (data == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return data.format(formatter);
    }

    private LocalDate parseData(String dataStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return LocalDate.parse(dataStr, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // Métodos de funcionalidades (mantidos do segundo código)
    private void mostrarMaisInformacoesAluno() {
        int selectedRow = tableAlunos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione um aluno para ver mais informações!");
            return;
        }

        int alunoId = (int) tableAlunos.getValueAt(selectedRow, 0);
        Aluno aluno = alunos.stream().filter(a -> a.getId() == alunoId).findFirst().orElse(null);

        if (aluno != null) {
            mostrarInformacoesCompletasAluno(aluno);
        }
    }

    private void mostrarInformacoesCompletasAluno(Aluno aluno) {
        JDialog dialog = new JDialog(frame, "Informações Completas - " + aluno.getNome(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(900, 700);
        dialog.setLocationRelativeTo(frame);

        JTabbedPane abas = new JTabbedPane();

        abas.addTab("Dados Básicos", criarAbaDadosBasicos(aluno));
        abas.addTab("Documentação", criarAbaDocumentacao(aluno));
        abas.addTab("Saúde", criarAbaSaude(aluno));
        abas.addTab("Situação Habitacional", criarAbaHabitacionalVisualizacao(aluno));
        abas.addTab("Bens da Família", criarAbaBensVisualizacao(aluno));
        abas.addTab("Composição Familiar", criarAbaComposicaoVisualizacao(aluno));
        abas.addTab("Responsáveis", criarAbaResponsaveis(aluno));

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dialog.dispose());

        JPanel panelBotoes = new JPanel();
        panelBotoes.add(btnFechar);

        dialog.add(abas, BorderLayout.CENTER);
        dialog.add(panelBotoes, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // Métodos de criação de abas (mantidos do segundo código)
    private JPanel criarAbaDadosBasicos(Aluno aluno) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Nome:"));
        panel.add(new JLabel(aluno.getNome()));
        panel.add(new JLabel("Data Nascimento:"));
        panel.add(new JLabel(formatarData(aluno.getDataNascimento())));
        panel.add(new JLabel("Idade:"));
        panel.add(new JLabel(aluno.getIdade() + " anos"));
        panel.add(new JLabel("CPF:"));
        panel.add(new JLabel(aluno.getCpf()));
        panel.add(new JLabel("Sexo:"));
        panel.add(new JLabel(aluno.getSexo()));
        panel.add(new JLabel("Cor/Raça:"));
        panel.add(new JLabel(aluno.getCorRaca()));
        panel.add(new JLabel("Turma:"));
        panel.add(new JLabel(aluno.getTurma() != null ? aluno.getTurma().getNome() : "Sem turma"));
        panel.add(new JLabel("Série:"));
        panel.add(new JLabel(aluno.getSerie() != null ? aluno.getSerie() : "Não informado"));
        panel.add(new JLabel("Ano:"));
        panel.add(new JLabel(aluno.getAno() != null ? aluno.getAno() : "Não informado"));

        return panel;
    }

    private JPanel criarAbaDocumentacao(Aluno aluno) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Documentacao doc = aluno.getDocumentacao();

        panel.add(new JLabel("Certidão de Nascimento:"));
        panel.add(new JLabel(doc.getCertidaoNascimento() != null ? doc.getCertidaoNascimento() : "Não informado"));
        panel.add(new JLabel("Município Nascimento:"));
        panel.add(new JLabel(doc.getMunicipioNascimento() != null ? doc.getMunicipioNascimento() : "Não informado"));
        panel.add(new JLabel("Município Registro:"));
        panel.add(new JLabel(doc.getMunicipioRegistro() != null ? doc.getMunicipioRegistro() : "Não informado"));
        panel.add(new JLabel("Cartório:"));
        panel.add(new JLabel(doc.getCartorio() != null ? doc.getCartorio() : "Não informado"));
        panel.add(new JLabel("RG:"));
        panel.add(new JLabel(doc.getRg() != null ? doc.getRg() : "Não informado"));
        panel.add(new JLabel("Data Emissão RG:"));
        panel.add(new JLabel(doc.getDataEmissaoRg() != null ? formatarData(doc.getDataEmissaoRg()) : "Não informado"));
        panel.add(new JLabel("Órgão Emissor:"));
        panel.add(new JLabel(doc.getOrgaoEmissor() != null ? doc.getOrgaoEmissor() : "Não informado"));

        return panel;
    }

    private JPanel criarAbaSaude(Aluno aluno) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Cadastro SUS:"));
        panel.add(new JLabel(aluno.getCadastroSus() != null ? aluno.getCadastroSus() : "Não informado"));
        panel.add(new JLabel("Unidade de Saúde:"));
        panel.add(new JLabel(aluno.getUnidadeSaude() != null ? aluno.getUnidadeSaude() : "Não informado"));
        panel.add(new JLabel("Problemas de Saúde:"));
        panel.add(new JLabel(aluno.getProblemasSaude() != null ? aluno.getProblemasSaude() : "Nenhum"));
        panel.add(new JLabel("Restrição Alimentar:"));
        panel.add(new JLabel(aluno.getRestricaoAlimentar() != null ? aluno.getRestricaoAlimentar() : "Nenhuma"));
        panel.add(new JLabel("Alergia:"));
        panel.add(new JLabel(aluno.getAlergia() != null ? aluno.getAlergia() : "Nenhuma"));
        panel.add(new JLabel("Mobilidade Reduzida:"));
        panel.add(new JLabel(aluno.getMobilidadeReduzida() != null ? aluno.getMobilidadeReduzida() : "Não"));
        panel.add(new JLabel("Deficiências Múltiplas:"));
        panel.add(new JLabel(aluno.isDeficienciasMultiplas() ? "Sim" : "Não"));
        panel.add(new JLabel("Público Educação Especial:"));
        panel.add(new JLabel(aluno.isPublicoAlvoEducacaoEspecial() ? "Sim" : "Não"));
        panel.add(new JLabel("Transporte Contratado:"));
        panel.add(new JLabel(aluno.isTransporteContratado() ? "Sim" : "Não"));

        if (!aluno.getClassificacoes().isEmpty()) {
            panel.add(new JLabel("Classificações Especiais:"));
            StringBuilder classificacoes = new StringBuilder();
            for (ClassificacaoNecessidadeEspecial classificacao : aluno.getClassificacoes()) {
                classificacoes.append(classificacao).append(", ");
            }
            panel.add(new JLabel(classificacoes.toString()));
        }

        return panel;
    }

    private JPanel criarAbaHabitacionalVisualizacao(Aluno aluno) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        SituacaoHabitacional habitacional = aluno.getSituacaoHabitacional();

        panel.add(new JLabel("Tipo de Moradia:"));
        panel.add(new JLabel(habitacional.getTipoMoradia() != null ? habitacional.getTipoMoradia().toString() : "Não informado"));
        panel.add(new JLabel("Valor Aluguel:"));
        panel.add(new JLabel("R$ " + habitacional.getValorAluguel()));
        panel.add(new JLabel("Número de Cômodos:"));
        panel.add(new JLabel(String.valueOf(habitacional.getNumeroComodos())));
        panel.add(new JLabel("Tipo de Piso:"));
        panel.add(new JLabel(habitacional.getTipoPiso() != null ? habitacional.getTipoPiso().toString() : "Não informado"));
        panel.add(new JLabel("Tipo de Construção:"));
        panel.add(new JLabel(habitacional.getTipoConstrucao() != null ? habitacional.getTipoConstrucao().toString() : "Não informado"));
        panel.add(new JLabel("Cobertura:"));
        panel.add(new JLabel(habitacional.getCobertura() != null ? habitacional.getCobertura().toString() : "Não informado"));
        panel.add(new JLabel("Fossa:"));
        panel.add(new JLabel(habitacional.isFossa() ? "Sim" : "Não"));
        panel.add(new JLabel("Cifon:"));
        panel.add(new JLabel(habitacional.isCifon() ? "Sim" : "Não"));
        panel.add(new JLabel("Energia Elétrica:"));
        panel.add(new JLabel(habitacional.isEnergiaEletrica() ? "Sim" : "Não"));
        panel.add(new JLabel("Água Encanada:"));
        panel.add(new JLabel(habitacional.isAguaEncanada() ? "Sim" : "Não"));

        return panel;
    }

    private JPanel criarAbaBensVisualizacao(Aluno aluno) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        BensFamiliares bens = aluno.getBens();

        String[] bensArray = {
                "TV", "DVD", "Rádio", "Computador", "Notebook", "Telefone Fixo",
                "Telefone Celular", "Tablet", "Internet", "TV Assinatura", "Fogão",
                "Geladeira", "Freezer", "Micro-ondas", "Máquina de Lavar", "Ar Condicionado",
                "Bicicleta", "Moto", "Automóvel"
        };

        boolean[] valores = {
                bens.isTv(), bens.isDvd(), bens.isRadio(), bens.isComputador(), bens.isNotebook(),
                bens.isTelefoneFixo(), bens.isTelefoneCelular(), bens.isTablet(), bens.isInternet(),
                bens.isTvAssinatura(), bens.isFogao(), bens.isGeladeira(), bens.isFreezer(),
                bens.isMicroOndas(), bens.isMaquinaLavar(), bens.isArCondicionado(),
                bens.isBicicleta(), bens.isMoto(), bens.isAutomovel()
        };

        for (int i = 0; i < bensArray.length; i++) {
            panel.add(new JLabel(bensArray[i] + ":"));
            panel.add(new JLabel(valores[i] ? "Sim" : "Não"));
        }

        return panel;
    }

    private JPanel criarAbaComposicaoVisualizacao(Aluno aluno) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ComposicaoFamiliar composicao = aluno.getComposicaoFamiliar();
        List<MembroFamilia> membros = composicao.getMembros();

        String[] colunas = {"Nome", "Idade", "Parentesco", "Escolaridade", "Emprego", "Renda (R$)"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);

        for (MembroFamilia membro : membros) {
            model.addRow(new Object[]{
                    membro.getNome(),
                    membro.getIdade(),
                    membro.getParentesco(),
                    membro.getSituacaoEscolar(),
                    membro.getSituacaoEmprego(),
                    membro.getRenda()
            });
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel panelResumo = new JPanel(new GridLayout(2, 2, 10, 10));
        panelResumo.add(new JLabel("Renda Familiar Total:"));
        panelResumo.add(new JLabel("R$ " + composicao.getRendaFamiliarTotal()));
        panelResumo.add(new JLabel("Renda Per Capita:"));
        panelResumo.add(new JLabel("R$ " + composicao.getRendaPerCapita()));

        panel.add(new JLabel("Composição Familiar (" + membros.size() + " membros):"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelResumo, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarAbaResponsaveis(Aluno aluno) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<Responsavel> responsaveisAluno = aluno.getResponsaveis();

        String[] colunas = {"Nome", "Idade", "Parentesco", "Telefone", "Celular", "Voluntariado"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);

        for (Responsavel resp : responsaveisAluno) {
            model.addRow(new Object[]{
                    resp.getNome(),
                    resp.getIdade(),
                    resp.getParentesco(),
                    resp.getTelefone(),
                    resp.getCelularWhatsapp(),
                    resp.getDiasVoluntariado() + " dias/mês"
            });
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(new JLabel("Responsáveis (" + responsaveisAluno.size() + "):"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        if (aluno.getResponsavelPrioritario() != null) {
            JLabel lblPrioritario = new JLabel("Responsável Prioritário: " +
                    aluno.getResponsavelPrioritario().getNome());
            panel.add(lblPrioritario, BorderLayout.SOUTH);
        }

        return panel;
    }

    // Métodos de edição (simplificados para exemplo)
    private void editarAluno() {
        int selectedRow = tableAlunos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione um aluno para editar!");
            return;
        }

        int alunoId = (int) tableAlunos.getValueAt(selectedRow, 0);
        Aluno aluno = alunos.stream().filter(a -> a.getId() == alunoId).findFirst().orElse(null);

        if (aluno != null) {
            // Implementar diálogo de edição similar ao cadastro
            JOptionPane.showMessageDialog(frame, "Funcionalidade de edição em desenvolvimento");
        }
    }

    private void editarFuncionario() {
        int selectedRow = tableFuncionarios.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione um funcionário para editar!");
            return;
        }
        JOptionPane.showMessageDialog(frame, "Funcionalidade de edição em desenvolvimento");
    }

    private void editarResponsavel() {
        int selectedRow = tableResponsaveis.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione um responsável para editar!");
            return;
        }
        JOptionPane.showMessageDialog(frame, "Funcionalidade de edição em desenvolvimento");
    }

    private void editarTurma() {
        int selectedRow = tableTurmas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione uma turma para editar!");
            return;
        }
        JOptionPane.showMessageDialog(frame, "Funcionalidade de edição em desenvolvimento");
    }

    private void editarMatricula() {
        int selectedRow = tableMatriculas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione uma matrícula para editar!");
            return;
        }
        JOptionPane.showMessageDialog(frame, "Funcionalidade de edição em desenvolvimento");
    }

    private void mostrarDetalhesAluno() {
        int selectedRow = tableAlunos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione um aluno para ver os detalhes!");
            return;
        }

        int alunoId = (int) tableAlunos.getValueAt(selectedRow, 0);
        Aluno alunoSelecionado = alunos.stream().filter(a -> a.getId() == alunoId).findFirst().orElse(null);

        if (alunoSelecionado != null) {
            StringBuilder detalhes = new StringBuilder();
            detalhes.append("=== DETALHES DO ALUNO ===\n\n");
            detalhes.append("Nome: ").append(alunoSelecionado.getNome()).append("\n");
            detalhes.append("Data Nascimento: ").append(formatarData(alunoSelecionado.getDataNascimento())).append("\n");
            detalhes.append("Idade: ").append(alunoSelecionado.getIdade()).append(" anos\n");
            detalhes.append("Sexo: ").append(alunoSelecionado.getSexo()).append("\n");
            detalhes.append("Cor/Raça: ").append(alunoSelecionado.getCorRaca()).append("\n");
            detalhes.append("CPF: ").append(alunoSelecionado.getCpf()).append("\n");
            detalhes.append("Problemas de Saúde: ").append(alunoSelecionado.getProblemasSaude()).append("\n");
            detalhes.append("Turma: ").append(alunoSelecionado.getTurma() != null ? alunoSelecionado.getTurma().getNome() : "Sem turma").append("\n");

            detalhes.append("\n=== RESPONSÁVEIS ===\n");
            for (Responsavel resp : alunoSelecionado.getResponsaveis()) {
                detalhes.append("• ").append(resp.getNome()).append(" (").append(resp.getParentesco()).append(") - ")
                        .append(resp.getTelefone()).append(" - ").append(resp.getDiasVoluntariado()).append(" dias voluntariado/mês\n");
            }

            JTextArea textArea = new JTextArea(detalhes.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            JOptionPane.showMessageDialog(frame, scrollPane, "Detalhes do Aluno", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void mostrarDetalhesTurma() {
        int selectedRow = tableTurmas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione uma turma para ver os detalhes!");
            return;
        }

        int turmaId = (int) tableTurmas.getValueAt(selectedRow, 0);
        Turma turmaSelecionada = turmas.stream().filter(t -> t.getId() == turmaId).findFirst().orElse(null);

        if (turmaSelecionada != null) {
            StringBuilder detalhes = new StringBuilder();
            detalhes.append("=== DETALHES DA TURMA ===\n\n");
            detalhes.append("Nome: ").append(turmaSelecionada.getNome()).append("\n");
            detalhes.append("Faixa Etária: ").append(turmaSelecionada.getFaixaEtaria()).append("\n");
            detalhes.append("Turno: ").append(turmaSelecionada.getTurno()).append("\n");
            detalhes.append("Professor: ").append(turmaSelecionada.getProfessor() != null ?
                    turmaSelecionada.getProfessor().getNome() : "Não atribuído").append("\n");

            if (turmaSelecionada instanceof TurmaCreche) {
                TurmaCreche creche = (TurmaCreche) turmaSelecionada;
                detalhes.append("Horário Lanche: ").append(creche.getHoraLancheManha()).append("\n");
                detalhes.append("Horário Almoço: ").append(creche.getHoraAlmoco()).append("\n");
                detalhes.append("Horário Cochilo: ").append(creche.getHoraCochilo()).append("\n");
                detalhes.append("Horário Banho: ").append(creche.getHoraBanho()).append("\n");
                detalhes.append("Horário Saída: ").append(creche.getHoraSaida()).append("\n");
            } else if (turmaSelecionada instanceof TurmaInfantil) {
                TurmaInfantil infantil = (TurmaInfantil) turmaSelecionada;
                detalhes.append("Horário Lanche: ").append(infantil.getHoraLancheManha()).append("\n");
                detalhes.append("Horário Almoço: ").append(infantil.getHoraAlmoco()).append("\n");
                detalhes.append("Horário Cochilo: ").append(infantil.getHoraCochilo()).append("\n");
                detalhes.append("Horário Banho: ").append(infantil.getHoraBanho()).append("\n");
                detalhes.append("Horário Saída: ").append(infantil.getHoraSaida()).append("\n");
            } else if (turmaSelecionada instanceof TurmaPre) {
                detalhes.append("Aulas Alfabetização: ").append(((TurmaPre) turmaSelecionada).getAulasAlfabetizacao()).append("\n");
            }

            detalhes.append("\n=== ALUNOS MATRICULADOS ===\n");
            detalhes.append("Total: ").append(turmaSelecionada.getAlunos().size()).append("/18 alunos\n\n");

            for (Aluno aluno : turmaSelecionada.getAlunos()) {
                detalhes.append("• ").append(aluno.getNome())
                        .append(" (").append(aluno.getIdade()).append(" anos)")
                        .append(" - Responsável: ").append(aluno.getResponsavelPrioritario().getNome())
                        .append("\n");
            }

            JTextArea textArea = new JTextArea(detalhes.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));

            JOptionPane.showMessageDialog(frame, scrollPane, "Detalhes da Turma", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void mostrarDetalhesCompletosMatricula() {
        int selectedRow = tableMatriculas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione uma matrícula para ver os detalhes!");
            return;
        }

        int numeroMatricula = (int) tableMatriculas.getValueAt(selectedRow, 0);
        Matricula matricula = matriculas.stream()
                .filter(m -> m.getNumeroMatricula() == numeroMatricula)
                .findFirst().orElse(null);

        if (matricula != null) {
            mostrarInformacoesCompletasMatricula(matricula);
        }
    }

    private void mostrarInformacoesCompletasMatricula(Matricula matricula) {
        JDialog dialog = new JDialog(frame, "Detalhes Completos da Matrícula - " + matricula.getAluno().getNome(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(1000, 700);
        dialog.setLocationRelativeTo(frame);

        JTabbedPane abas = new JTabbedPane();
        Aluno aluno = matricula.getAluno();

        abas.addTab("Dados da Matrícula", criarAbaDadosMatricula(matricula));
        abas.addTab("Dados do Aluno", criarAbaDadosBasicos(aluno));
        abas.addTab("Situação Socioeconômica", criarAbaSocioeconomica(aluno));
        abas.addTab("Responsáveis", criarAbaResponsaveisMatricula(matricula));

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dialog.dispose());

        JPanel panelBotoes = new JPanel();
        panelBotoes.add(btnFechar);

        dialog.add(abas, BorderLayout.CENTER);
        dialog.add(panelBotoes, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel criarAbaDadosMatricula(Matricula matricula) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Número Matrícula:"));
        panel.add(new JLabel(String.valueOf(matricula.getNumeroMatricula())));
        panel.add(new JLabel("Data:"));
        panel.add(new JLabel(formatarData(matricula.getData())));
        panel.add(new JLabel("Situação:"));
        panel.add(new JLabel(matricula.getSituacao().toString()));
        panel.add(new JLabel("Endereço:"));
        panel.add(new JLabel(matricula.getEndereco()));
        panel.add(new JLabel("Observações:"));
        panel.add(new JLabel(matricula.getObservacoes() != null ? matricula.getObservacoes() : "Nenhuma"));
        panel.add(new JLabel("Funcionário Responsável:"));
        panel.add(new JLabel(matricula.getFuncionario().getNome()));
        panel.add(new JLabel("Turma:"));
        panel.add(new JLabel(matricula.getTurma() != null ? matricula.getTurma().getNome() : "Não definida"));
        panel.add(new JLabel("Orientações Recebidas:"));
        panel.add(new JLabel(matricula.isDeclaracaoOrientacoes() ?
                "Sim - " + formatarData(matricula.getDataDeclaracao()) : "Não"));

        return panel;
    }

    private JPanel criarAbaSocioeconomica(Aluno aluno) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane subAbas = new JTabbedPane();
        subAbas.addTab("Situação Habitacional", criarAbaHabitacionalVisualizacao(aluno));
        subAbas.addTab("Bens da Família", criarAbaBensVisualizacao(aluno));
        subAbas.addTab("Composição Familiar", criarAbaComposicaoVisualizacao(aluno));

        panel.add(subAbas, BorderLayout.CENTER);
        return panel;
    }

    private JPanel criarAbaResponsaveisMatricula(Matricula matricula) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<Responsavel> responsaveisMatricula = matricula.getResponsaveis();

        String[] colunas = {"Nome", "Idade", "Parentesco", "Telefone", "Celular", "Voluntariado", "CPF"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);

        for (Responsavel resp : responsaveisMatricula) {
            model.addRow(new Object[]{
                    resp.getNome(),
                    resp.getIdade(),
                    resp.getParentesco(),
                    resp.getTelefone(),
                    resp.getCelularWhatsapp(),
                    resp.getDiasVoluntariado() + " dias/mês",
                    resp.getCpf()
            });
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(new JLabel("Responsáveis da Matrícula (" + responsaveisMatricula.size() + "):"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void editarDadosSocioeconomicos() {
        int selectedRow = tableMatriculas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione uma matrícula para editar os dados socioeconômicos!");
            return;
        }
        JOptionPane.showMessageDialog(frame, "Funcionalidade de edição socioeconômica em desenvolvimento");
    }

    private void completarDadosSocioeconomicos() {
        int selectedRow = tableMatriculas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione uma matrícula para completar os dados!");
            return;
        }
        JOptionPane.showMessageDialog(frame, "Funcionalidade de completar dados em desenvolvimento");
    }

    private void ativarMatricula() {
        int selectedRow = tablePreMatriculas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Selecione uma pré-matrícula para ativar!");
            return;
        }

        int numeroMatricula = (int) tablePreMatriculas.getValueAt(selectedRow, 0);
        Matricula preMatricula = matriculas.stream()
                .filter(mat -> mat.getNumeroMatricula() == numeroMatricula &&
                        mat.getSituacao() == SituacaoMatricula.PRE_MATRICULA)
                .findFirst().orElse(null);

        if (preMatricula != null) {
            ativarMatriculaComDadosSocioeconomicos(preMatricula);
        }
    }

    private void ativarMatriculaComDadosSocioeconomicos(Matricula preMatricula) {
        JDialog dialog = new JDialog(frame, "Completar Matrícula - Dados Socioeconômicos", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(900, 700);
        dialog.setLocationRelativeTo(frame);

        JTabbedPane abas = new JTabbedPane();
        Aluno aluno = preMatricula.getAluno();

        JPanel abaDadosMatricula = new JPanel(new GridLayout(0, 2, 10, 10));
        abaDadosMatricula.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblAluno = new JLabel("Aluno: " + aluno.getNome());
        JLabel lblIdade = new JLabel("Idade: " + aluno.getIdade() + " anos");
        JComboBox<Turma> cmbTurma = new JComboBox<>();
        JComboBox<SituacaoMatricula> cmbSituacao = new JComboBox<>(new SituacaoMatricula[]{
                SituacaoMatricula.PENDENTE_DADOS, SituacaoMatricula.ATIVA
        });
        cmbSituacao.setSelectedItem(SituacaoMatricula.PENDENTE_DADOS);

        for (Turma turma : turmas) {
            cmbTurma.addItem(turma);
        }

        abaDadosMatricula.add(new JLabel("Informações do Aluno:"));
        abaDadosMatricula.add(new JLabel());
        abaDadosMatricula.add(lblAluno);
        abaDadosMatricula.add(lblIdade);
        abaDadosMatricula.add(new JLabel("Turma:*"));
        abaDadosMatricula.add(cmbTurma);
        abaDadosMatricula.add(new JLabel("Situação Final:*"));
        abaDadosMatricula.add(cmbSituacao);

        abas.addTab("Dados da Matrícula", abaDadosMatricula);
        abas.addTab("Situação Habitacional", criarAbaHabitacionalEdicao(aluno));
        abas.addTab("Bens da Família", criarAbaBensEdicao(aluno));
        abas.addTab("Composição Familiar", criarAbaComposicaoEdicao(aluno));

        JButton btnSalvar = new JButton("Salvar Matrícula Completa");
        JButton btnCancelar = new JButton("Cancelar");

        btnSalvar.addActionListener(e -> {
            Turma turmaSelecionada = (Turma) cmbTurma.getSelectedItem();
            SituacaoMatricula novaSituacao = (SituacaoMatricula) cmbSituacao.getSelectedItem();

            if (turmaSelecionada == null) {
                JOptionPane.showMessageDialog(dialog, "Selecione uma turma!");
                return;
            }

            if (turmaSelecionada.getAlunos().size() >= 18) {
                JOptionPane.showMessageDialog(dialog, "Turma lotada! Limite de 18 alunos.");
                return;
            }

            if (!turmaSelecionada.verificarIdadeAluno(aluno.getIdade())) {
                JOptionPane.showMessageDialog(dialog,
                        "Idade do aluno não é compatível com a turma!\n\n" +
                                "Aluno: " + aluno.getNome() + " (" + aluno.getIdade() + " anos)\n" +
                                "Turma: " + turmaSelecionada.getNome() + " (" + turmaSelecionada.getFaixaEtaria() + ")");
                return;
            }

            if (aluno.getTurma() != null) {
                JOptionPane.showMessageDialog(dialog,
                        "Este aluno já está em uma turma!\n\n" +
                                "Aluno: " + aluno.getNome() + "\n" +
                                "Turma atual: " + aluno.getTurma().getNome());
                return;
            }

            int confirmacao = JOptionPane.showConfirmDialog(dialog,
                    "Confirmar ativação da matrícula?\n\n" +
                            "Aluno: " + aluno.getNome() + "\n" +
                            "Turma: " + turmaSelecionada.getNome() + "\n" +
                            "Situação: " + novaSituacao + "\n" +
                            "Dados socioeconômicos: COMPLETOS",
                    "Confirmar Matrícula",
                    JOptionPane.YES_NO_OPTION);

            if (confirmacao == JOptionPane.YES_OPTION) {
                preMatricula.setSituacao(novaSituacao);
                preMatricula.setTurma(turmaSelecionada);

                boolean alunoAdicionado = turmaSelecionada.adicionarAluno(aluno);

                if (alunoAdicionado) {
                    for (Responsavel resp : preMatricula.getResponsaveis()) {
                        long filhosNaCreche = alunos.stream()
                                .filter(a -> a.getResponsaveis().contains(resp) && a.getTurma() != null)
                                .count();
                        resp.setDiasVoluntariado((int) filhosNaCreche);
                    }

                    try {
                        matriculaDAO.atualizar(preMatricula);
                        turmaDAO.adicionarAlunoTurma(turmaSelecionada.getId(), aluno.getId());

                        // Recarregar dados do banco
                        matriculas = matriculaDAO.listarTodos();
                        turmas = turmaDAO.listarTodos();
                        alunos = alunoDAO.listarTodos();
                        responsaveis = responsavelDAO.listarTodos();

                        atualizarTabelaMatriculas();
                        atualizarTabelaPreMatriculas();
                        atualizarTabelaTurmas();
                        atualizarTabelaAlunos();
                        atualizarTabelaResponsaveis();

                        dialog.dispose();

                        String mensagem = novaSituacao == SituacaoMatricula.ATIVA ?
                                "✅ MATRÍCULA ATIVADA COM SUCESSO!\n\n" :
                                "✅ MATRÍCULA COMPLETADA - AGUARDANDO APROVAÇÃO!\n\n";

                        mensagem += "Aluno: " + aluno.getNome() + "\n" +
                                "Turma: " + turmaSelecionada.getNome() + "\n" +
                                "Situação: " + novaSituacao + "\n" +
                                "Dados socioeconômicos: PREENCHIDOS";

                        JOptionPane.showMessageDialog(frame, mensagem,
                                novaSituacao == SituacaoMatricula.ATIVA ? "Matrícula Ativada" : "Matrícula Pendente",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog, "Erro ao salvar no banco: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Erro ao adicionar aluno à turma!");
                }
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        JPanel panelBotoes = new JPanel();
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnCancelar);

        dialog.add(abas, BorderLayout.CENTER);
        dialog.add(panelBotoes, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // Métodos de criação de abas de edição (simplificados)
    private JPanel criarAbaHabitacionalEdicao(Aluno aluno) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        SituacaoHabitacional habitacional = aluno.getSituacaoHabitacional();

        JComboBox<TipoMoradia> cmbTipoMoradia = new JComboBox<>(TipoMoradia.values());
        JTextField txtValorAluguel = new JTextField(String.valueOf(habitacional.getValorAluguel()));
        JTextField txtNumeroComodos = new JTextField(String.valueOf(habitacional.getNumeroComodos()));
        JComboBox<TipoPiso> cmbTipoPiso = new JComboBox<>(TipoPiso.values());
        JComboBox<TipoConstrucao> cmbTipoConstrucao = new JComboBox<>(TipoConstrucao.values());
        JComboBox<TipoCobertura> cmbCobertura = new JComboBox<>(TipoCobertura.values());
        JCheckBox chkFossa = new JCheckBox("", habitacional.isFossa());
        JCheckBox chkCifon = new JCheckBox("", habitacional.isCifon());
        JCheckBox chkEnergia = new JCheckBox("", habitacional.isEnergiaEletrica());
        JCheckBox chkAgua = new JCheckBox("", habitacional.isAguaEncanada());

        if (habitacional.getTipoMoradia() != null) cmbTipoMoradia.setSelectedItem(habitacional.getTipoMoradia());
        if (habitacional.getTipoPiso() != null) cmbTipoPiso.setSelectedItem(habitacional.getTipoPiso());
        if (habitacional.getTipoConstrucao() != null) cmbTipoConstrucao.setSelectedItem(habitacional.getTipoConstrucao());
        if (habitacional.getCobertura() != null) cmbCobertura.setSelectedItem(habitacional.getCobertura());

        panel.add(new JLabel("Tipo de Moradia:"));
        panel.add(cmbTipoMoradia);
        panel.add(new JLabel("Valor Aluguel (R$):"));
        panel.add(txtValorAluguel);
        panel.add(new JLabel("Número de Cômodos:"));
        panel.add(txtNumeroComodos);
        panel.add(new JLabel("Tipo de Piso:"));
        panel.add(cmbTipoPiso);
        panel.add(new JLabel("Tipo de Construção:"));
        panel.add(cmbTipoConstrucao);
        panel.add(new JLabel("Cobertura:"));
        panel.add(cmbCobertura);
        panel.add(new JLabel("Tem Fossa:"));
        panel.add(chkFossa);
        panel.add(new JLabel("Tem Cifon:"));
        panel.add(chkCifon);
        panel.add(new JLabel("Tem Energia Elétrica:"));
        panel.add(chkEnergia);
        panel.add(new JLabel("Tem Água Encanada:"));
        panel.add(chkAgua);

        return panel;
    }

    private JPanel criarAbaBensEdicao(Aluno aluno) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        BensFamiliares bens = aluno.getBens();

        JCheckBox chkTv = new JCheckBox("", bens.isTv());
        JCheckBox chkDvd = new JCheckBox("", bens.isDvd());
        JCheckBox chkRadio = new JCheckBox("", bens.isRadio());
        JCheckBox chkComputador = new JCheckBox("", bens.isComputador());
        JCheckBox chkNotebook = new JCheckBox("", bens.isNotebook());
        JCheckBox chkTelefoneFixo = new JCheckBox("", bens.isTelefoneFixo());
        JCheckBox chkTelefoneCelular = new JCheckBox("", bens.isTelefoneCelular());
        JCheckBox chkTablet = new JCheckBox("", bens.isTablet());
        JCheckBox chkInternet = new JCheckBox("", bens.isInternet());
        JCheckBox chkTvAssinatura = new JCheckBox("", bens.isTvAssinatura());
        JCheckBox chkFogao = new JCheckBox("", bens.isFogao());
        JCheckBox chkGeladeira = new JCheckBox("", bens.isGeladeira());
        JCheckBox chkFreezer = new JCheckBox("", bens.isFreezer());
        JCheckBox chkMicroOndas = new JCheckBox("", bens.isMicroOndas());
        JCheckBox chkMaquinaLavar = new JCheckBox("", bens.isMaquinaLavar());
        JCheckBox chkArCondicionado = new JCheckBox("", bens.isArCondicionado());
        JCheckBox chkBicicleta = new JCheckBox("", bens.isBicicleta());
        JCheckBox chkMoto = new JCheckBox("", bens.isMoto());
        JCheckBox chkAutomovel = new JCheckBox("", bens.isAutomovel());

        panel.add(new JLabel("TV:"));
        panel.add(chkTv);
        panel.add(new JLabel("DVD:"));
        panel.add(chkDvd);
        panel.add(new JLabel("Rádio:"));
        panel.add(chkRadio);
        panel.add(new JLabel("Computador:"));
        panel.add(chkComputador);
        panel.add(new JLabel("Notebook:"));
        panel.add(chkNotebook);
        panel.add(new JLabel("Telefone Fixo:"));
        panel.add(chkTelefoneFixo);
        panel.add(new JLabel("Telefone Celular:"));
        panel.add(chkTelefoneCelular);
        panel.add(new JLabel("Tablet:"));
        panel.add(chkTablet);
        panel.add(new JLabel("Internet:"));
        panel.add(chkInternet);
        panel.add(new JLabel("TV Assinatura:"));
        panel.add(chkTvAssinatura);
        panel.add(new JLabel("Fogão:"));
        panel.add(chkFogao);
        panel.add(new JLabel("Geladeira:"));
        panel.add(chkGeladeira);
        panel.add(new JLabel("Freezer:"));
        panel.add(chkFreezer);
        panel.add(new JLabel("Micro-ondas:"));
        panel.add(chkMicroOndas);
        panel.add(new JLabel("Máquina de Lavar:"));
        panel.add(chkMaquinaLavar);
        panel.add(new JLabel("Ar Condicionado:"));
        panel.add(chkArCondicionado);
        panel.add(new JLabel("Bicicleta:"));
        panel.add(chkBicicleta);
        panel.add(new JLabel("Moto:"));
        panel.add(chkMoto);
        panel.add(new JLabel("Automóvel:"));
        panel.add(chkAutomovel);

        return panel;
    }

    private JPanel criarAbaComposicaoEdicao(Aluno aluno) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ComposicaoFamiliar composicao = aluno.getComposicaoFamiliar();

        String[] colunas = {"Nome", "Idade", "Parentesco", "Escolaridade", "Emprego", "Renda (R$)"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        for (MembroFamilia membro : composicao.getMembros()) {
            model.addRow(new Object[]{
                    membro.getNome(),
                    membro.getIdade(),
                    membro.getParentesco(),
                    membro.getSituacaoEscolar(),
                    membro.getSituacaoEmprego(),
                    membro.getRenda()
            });
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel panelBotoes = new JPanel();
        JButton btnAdicionar = new JButton("Adicionar Membro");
        JButton btnRemover = new JButton("Remover Membro");
        JButton btnAtualizar = new JButton("Atualizar Rendas");

        btnAdicionar.addActionListener(e -> model.addRow(new Object[]{"", 0, "", "", "", 0.0}));
        btnRemover.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                model.removeRow(selectedRow);
            }
        });

        btnAtualizar.addActionListener(e -> {
            composicao.getMembros().clear();
            for (int i = 0; i < model.getRowCount(); i++) {
                String nome = (String) model.getValueAt(i, 0);
                int idade = (int) model.getValueAt(i, 1);
                String parentesco = (String) model.getValueAt(i, 2);
                String escolaridade = (String) model.getValueAt(i, 3);
                String emprego = (String) model.getValueAt(i, 4);
                double renda = (double) model.getValueAt(i, 5);

                MembroFamilia membro = new MembroFamilia(nome, idade, parentesco, escolaridade, emprego, renda);
                composicao.adicionarMembro(membro);
            }
            JOptionPane.showMessageDialog(panel, "Rendas atualizadas!");
        });

        panelBotoes.add(btnAdicionar);
        panelBotoes.add(btnRemover);
        panelBotoes.add(btnAtualizar);

        JPanel panelResumo = new JPanel(new GridLayout(2, 2, 10, 10));
        panelResumo.add(new JLabel("Renda Familiar Total:"));
        panelResumo.add(new JLabel("R$ " + composicao.getRendaFamiliarTotal()));
        panelResumo.add(new JLabel("Renda Per Capita:"));
        panelResumo.add(new JLabel("R$ " + composicao.getRendaPerCapita()));

        panel.add(new JLabel("Composição Familiar (Edição):"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBotoes, BorderLayout.SOUTH);

        return panel;
    }

    // Métodos de relatórios (mantidos do segundo código)
    private void gerarRelatorioAlunos() {
        try {
            File pastaRelatorios = new File("relatorios");
            if (!pastaRelatorios.exists()) {
                pastaRelatorios.mkdirs();
            }

            String nomeArquivo = "relatorios/relatorio_alunos_" + System.currentTimeMillis() + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo));

            writer.println("=".repeat(60));
            writer.println("          RELATÓRIO DE ALUNOS - CRECHE ESTRELA DO ORIENTE");
            writer.println("=".repeat(60));
            writer.println("Gerado em: " + LocalDate.now());
            writer.println();

            writer.println("-".repeat(120));
            writer.printf("%-5s %-20s %-12s %-6s %-10s %-15s %-20s %-15s%n",
                    "ID", "Nome", "Nascimento", "Idade", "Sexo", "Turma", "Responsável", "Voluntariado");
            writer.println("-".repeat(120));

            for (Aluno aluno : alunos) {
                writer.printf("%-5d %-20s %-12s %-6d %-10s %-15s %-20s %-15s%n",
                        aluno.getId(),
                        aluno.getNome(),
                        formatarData(aluno.getDataNascimento()),
                        aluno.getIdade(),
                        aluno.getSexo(),
                        aluno.getTurma() != null ? aluno.getTurma().getNome() : "Sem turma",
                        aluno.getResponsavelPrioritario() != null ? aluno.getResponsavelPrioritario().getNome() : "Nenhum",
                        aluno.getResponsavelPrioritario() != null ?
                                aluno.getResponsavelPrioritario().getDiasVoluntariado() + " dias/mês" : "0 dias/mês");
            }

            writer.println("-".repeat(120));
            writer.println();

            long totalMeninos = alunos.stream().filter(a -> "Masculino".equals(a.getSexo())).count();
            long totalMeninas = alunos.stream().filter(a -> "Feminino".equals(a.getSexo())).count();
            long comTurma = alunos.stream().filter(a -> a.getTurma() != null).count();
            long semTurma = alunos.stream().filter(a -> a.getTurma() == null).count();

            writer.println("ESTATÍSTICAS:");
            writer.println("Total de alunos: " + alunos.size());
            writer.println("Meninos: " + totalMeninos);
            writer.println("Meninas: " + totalMeninas);
            writer.println("Com turma: " + comTurma);
            writer.println("Sem turma: " + semTurma);

            writer.close();

            JOptionPane.showMessageDialog(frame,
                    "Relatório de alunos gerado com sucesso!\n" +
                            "Arquivo: " + nomeArquivo,
                    "Relatório Gerado",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gerarRelatorioFuncionarios() {
        try {
            File pastaRelatorios = new File("relatorios");
            if (!pastaRelatorios.exists()) {
                pastaRelatorios.mkdirs();
            }

            String nomeArquivo = "relatorios/relatorio_funcionarios_" + System.currentTimeMillis() + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo));

            writer.println("=".repeat(60));
            writer.println("       RELATÓRIO DE FUNCIONÁRIOS - CRECHE ESTRELA DO ORIENTE");
            writer.println("=".repeat(60));
            writer.println("Gerado em: " + LocalDate.now());
            writer.println();

            writer.println("-".repeat(130));
            writer.printf("%-5s %-20s %-8s %-15s %-15s %-15s %-12s %-20s%n",
                    "ID", "Nome", "Idade", "CPF", "Cargo", "Vínculo", "Tipo", "Detalhes");
            writer.println("-".repeat(130));

            for (Funcionario func : funcionarios) {
                String detalhes = "";
                if (func instanceof Professor) {
                    detalhes = "Turno: " + ((Professor) func).getTurno();
                } else if (func instanceof Assistente) {
                    detalhes = "Faixa: " + ((Assistente) func).getFaixaEtariaAtendida();
                } else if (func instanceof Coordenador) {
                    detalhes = "Setor: " + ((Coordenador) func).getSetorResponsavel();
                }

                writer.printf("%-5d %-20s %-8d %-15s %-15s %-15s %-12s %-20s%n",
                        func.getId(),
                        func.getNome(),
                        func.getIdade(),
                        func.getCpf(),
                        func.getCargo(),
                        func.getVinculo(),
                        func.isVoluntario() ? "Voluntário" : "Remunerado",
                        detalhes);
            }

            writer.println("-".repeat(130));
            writer.println();

            int professores = 0, assistentes = 0, coordenadores = 0;
            int voluntarios = 0, remunerados = 0;

            for (Funcionario func : funcionarios) {
                if (func instanceof Professor) professores++;
                else if (func instanceof Assistente) assistentes++;
                else if (func instanceof Coordenador) coordenadores++;

                if (func.isVoluntario()) voluntarios++;
                else remunerados++;
            }

            writer.println("ESTATÍSTICAS:");
            writer.println("Total de funcionários: " + funcionarios.size());
            writer.println("Professores: " + professores);
            writer.println("Assistentes: " + assistentes);
            writer.println("Coordenadores: " + coordenadores);
            writer.println("Voluntários: " + voluntarios);
            writer.println("Remunerados: " + remunerados);

            writer.close();

            JOptionPane.showMessageDialog(frame,
                    "Relatório de funcionários gerado com sucesso!\n" +
                            "Arquivo: " + nomeArquivo,
                    "Relatório Gerado",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gerarRelatorioTurmas() {
        try {
            File pastaRelatorios = new File("relatorios");
            if (!pastaRelatorios.exists()) {
                pastaRelatorios.mkdirs();
            }

            String nomeArquivo = "relatorios/relatorio_turmas_" + System.currentTimeMillis() + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo));

            writer.println("=".repeat(60));
            writer.println("         RELATÓRIO DE TURMAS - CRECHE ESTRELA DO ORIENTE");
            writer.println("=".repeat(60));
            writer.println("Gerado em: " + LocalDate.now());
            writer.println();

            writer.println("-".repeat(100));
            writer.printf("%-5s %-15s %-15s %-10s %-20s %-8s%n",
                    "ID", "Nome", "Faixa Etária", "Turno", "Professor", "Alunos");
            writer.println("-".repeat(100));

            for (Turma turma : turmas) {
                writer.printf("%-5d %-15s %-15s %-10s %-20s %-8d%n",
                        turma.getId(),
                        turma.getNome(),
                        turma.getFaixaEtaria(),
                        turma.getTurno(),
                        turma.getProfessor() != null ? turma.getProfessor().getNome() : "Não atribuído",
                        turma.getAlunos().size());
            }

            writer.println("-".repeat(100));
            writer.println();

            int totalAlunos = 0;
            int turmasCreche = 0, turmasInfantil = 0, turmasPre = 0;

            for (Turma turma : turmas) {
                totalAlunos += turma.getAlunos().size();
                if (turma instanceof TurmaCreche) turmasCreche++;
                else if (turma instanceof TurmaInfantil) turmasInfantil++;
                else if (turma instanceof TurmaPre) turmasPre++;
            }

            writer.println("ESTATÍSTICAS:");
            writer.println("Total de turmas: " + turmas.size());
            writer.println("Turmas Creche (2-3 anos): " + turmasCreche);
            writer.println("Turmas Infantil (4-5 anos): " + turmasInfantil);
            writer.println("Turmas Pré (6 anos): " + turmasPre);
            writer.println("Total de alunos matriculados: " + totalAlunos);
            writer.println("Média de alunos por turma: " + (turmas.isEmpty() ? 0 : totalAlunos / turmas.size()));
            writer.println("Vagas disponíveis: " + (turmas.size() * 18 - totalAlunos));

            writer.close();

            JOptionPane.showMessageDialog(frame,
                    "Relatório de turmas gerado com sucesso!\n" +
                            "Arquivo: " + nomeArquivo,
                    "Relatório Gerado",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gerarRelatorioMatriculas() {
        try {
            File pastaRelatorios = new File("relatorios");
            if (!pastaRelatorios.exists()) {
                pastaRelatorios.mkdirs();
            }

            String nomeArquivo = "relatorios/relatorio_matriculas_" + System.currentTimeMillis() + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo));

            writer.println("=".repeat(60));
            writer.println("        RELATÓRIO DE MATRÍCULAS - CRECHE ESTRELA DO ORIENTE");
            writer.println("=".repeat(60));
            writer.println("Gerado em: " + LocalDate.now());
            writer.println();

            writer.println("-".repeat(120));
            writer.printf("%-10s %-12s %-12s %-15s %-15s %-20s %-12s%n",
                    "Número", "Data", "Situação", "Aluno", "Turma", "Funcionário", "Orientação");
            writer.println("-".repeat(120));

            for (Matricula mat : matriculas) {
                writer.printf("%-10d %-12s %-12s %-15s %-15s %-20s %-12s%n",
                        mat.getNumeroMatricula(),
                        formatarData(mat.getData()),
                        mat.getSituacao(),
                        mat.getAluno().getNome(),
                        mat.getTurma() != null ? mat.getTurma().getNome() : "Não definida",
                        mat.getFuncionario().getNome(),
                        mat.isDeclaracaoOrientacoes() ? "Recebida" : "Pendente");
            }

            writer.println("-".repeat(120));
            writer.println();

            int preMatriculas = 0, ativas = 0, inativas = 0, pendentes = 0, concluidas = 0, desistentes = 0;
            for (Matricula mat : matriculas) {
                switch (mat.getSituacao()) {
                    case PRE_MATRICULA: preMatriculas++; break;
                    case ATIVA: ativas++; break;
                    case INATIVA: inativas++; break;
                    case PENDENTE_DADOS: pendentes++; break;
                    case CONCLUIDA: concluidas++; break;
                    case DESISTENTE: desistentes++; break;
                }
            }

            writer.println("ESTATÍSTICAS:");
            writer.println("Total de matrículas: " + matriculas.size());
            writer.println("Pré-matrículas: " + preMatriculas);
            writer.println("Ativas: " + ativas);
            writer.println("Inativas: " + inativas);
            writer.println("Pendentes: " + pendentes);
            writer.println("Concluídas: " + concluidas);
            writer.println("Desistentes: " + desistentes);

            writer.close();

            JOptionPane.showMessageDialog(frame,
                    "Relatório de matrículas gerado com sucesso!\n" +
                            "Arquivo: " + nomeArquivo,
                    "Relatório Gerado",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gerarRelatorioSocioeconomico() {
        try {
            File pastaRelatorios = new File("relatorios");
            if (!pastaRelatorios.exists()) {
                pastaRelatorios.mkdirs();
            }

            String nomeArquivo = "relatorios/relatorio_socioeconomico_" + System.currentTimeMillis() + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo));

            writer.println("=".repeat(60));
            writer.println("     RELATÓRIO SOCIOECONÔMICO - CRECHE ESTRELA DO ORIENTE");
            writer.println("=".repeat(60));
            writer.println("Gerado em: " + LocalDate.now());
            writer.println();

            int totalDiasVoluntariado = responsaveis.stream().mapToInt(Responsavel::getDiasVoluntariado).sum();
            long responsaveisComVoluntariado = responsaveis.stream().filter(r -> r.getDiasVoluntariado() > 0).count();

            writer.println("VOLUNTARIADO:");
            writer.println("Total de dias de voluntariado/mês: " + totalDiasVoluntariado);
            writer.println("Responsáveis com voluntariado: " + responsaveisComVoluntariado);
            writer.println("Responsáveis sem voluntariado: " + (responsaveis.size() - responsaveisComVoluntariado));
            writer.println();

            long alunosComNecessidades = alunos.stream()
                    .filter(a -> !a.getClassificacoes().isEmpty())
                    .count();

            writer.println("NECESSIDADES ESPECIAIS:");
            writer.println("Alunos com necessidades especiais: " + alunosComNecessidades);
            writer.println("Alunos sem necessidades especiais: " + (alunos.size() - alunosComNecessidades));
            writer.println();

            long alunosComTransporte = alunos.stream().filter(Aluno::isTransporteContratado).count();

            writer.println("TRANSPORTE:");
            writer.println("Alunos com transporte contratado: " + alunosComTransporte);
            writer.println("Alunos sem transporte contratado: " + (alunos.size() - alunosComTransporte));

            writer.close();

            JOptionPane.showMessageDialog(frame,
                    "Relatório socioeconômico gerado com sucesso!\n" +
                            "Arquivo: " + nomeArquivo,
                    "Relatório Gerado",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gerarRelatorioCompleto() {
        try {
            File pastaRelatorios = new File("relatorios");
            if (!pastaRelatorios.exists()) {
                pastaRelatorios.mkdirs();
            }

            String nomeArquivo = "relatorios/relatorio_completo_" + System.currentTimeMillis() + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo));

            writer.println("=".repeat(70));
            writer.println("           RELATÓRIO COMPLETO - CRECHE ESTRELA DO ORIENTE");
            writer.println("=".repeat(70));
            writer.println("Gerado em: " + LocalDate.now());
            writer.println();

            int totalAlunosTurmas = 0;
            for (Turma turma : turmas) {
                totalAlunosTurmas += turma.getAlunos().size();
            }

            int totalVoluntarios = (int) funcionarios.stream().filter(Funcionario::isVoluntario).count();
            int totalRemunerados = (int) funcionarios.stream().filter(f -> !f.isVoluntario()).count();
            int totalDiasVoluntariado = responsaveis.stream().mapToInt(Responsavel::getDiasVoluntariado).sum();

            writer.println("RESUMO GERAL DA CRECHE:");
            writer.println("Total de alunos: " + alunos.size());
            writer.println("Total de funcionários: " + funcionarios.size() + " (" + totalRemunerados + " remunerados, " + totalVoluntarios + " voluntários)");
            writer.println("Total de responsáveis: " + responsaveis.size());
            writer.println("Total de turmas: " + turmas.size());
            writer.println("Total de matrículas: " + matriculas.size());
            writer.println("Alunos em turmas: " + totalAlunosTurmas);
            writer.println("Total dias voluntariado/mês: " + totalDiasVoluntariado);
            writer.println("Vagas disponíveis: " + (turmas.size() * 18 - totalAlunosTurmas));
            writer.println();

            writer.println("=".repeat(50));
            writer.println("DISTRIBUIÇÃO POR TURMAS");
            writer.println("=".repeat(50));
            for (Turma turma : turmas) {
                writer.println("• " + turma.getNome() + " (" + turma.getFaixaEtaria() + "): " +
                        turma.getAlunos().size() + "/18 alunos - " +
                        (turma.getProfessor() != null ? turma.getProfessor().getNome() : "Sem professor"));
            }
            writer.println();

            writer.println("=".repeat(50));
            writer.println("FUNCIONÁRIOS VOLUNTÁRIOS");
            writer.println("=".repeat(50));
            for (Funcionario func : funcionarios) {
                if (func.isVoluntario()) {
                    writer.println("• " + func.getNome() + " - " + func.getCargo() + " - " + func.getVinculo());
                }
            }

            writer.close();

            JOptionPane.showMessageDialog(frame,
                    "Relatório completo gerado com sucesso!\n" +
                            "Arquivo: " + nomeArquivo,
                    "Relatório Gerado",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cadastrarDadosIniciais() {
        // Cadastra dados iniciais apenas se o banco estiver vazio
        if (alunos.isEmpty() && funcionarios.isEmpty() && responsaveis.isEmpty()) {
            try {
                // Responsáveis
                Responsavel resp1 = new Responsavel(nextResponsavelId++, "Maria Silva", 35, "111.222.333-44", "Mãe", "(11) 99999-1111", "(11) 98888-1111");
                Responsavel resp2 = new Responsavel(nextResponsavelId++, "João Santos", 40, "222.333.444-55", "Pai", "(11) 99999-2222", "(11) 98888-2222");
                Responsavel resp3 = new Responsavel(nextResponsavelId++, "Ana Oliveira", 28, "333.444.555-66", "Mãe", "(11) 99999-3333", "(11) 98888-3333");

                responsavelDAO.cadastrar(resp1);
                responsavelDAO.cadastrar(resp2);
                responsavelDAO.cadastrar(resp3);

                responsaveis = responsavelDAO.listarTodos();

                // Alunos
                Aluno aluno1 = new Aluno(nextAlunoId++, "Lucas Silva", LocalDate.of(2021, 3, 15), "123.456.789-00", "Masculino", "Branco", "");
                Aluno aluno2 = new Aluno(nextAlunoId++, "Julia Santos", LocalDate.of(2020, 8, 22), "234.567.890-11", "Feminino", "Pardo", "");
                Aluno aluno3 = new Aluno(nextAlunoId++, "Miguel Oliveira", LocalDate.of(2019, 11, 5), "345.678.901-22", "Masculino", "Negro", "");

                aluno1.adicionarResponsavel(resp1);
                aluno2.adicionarResponsavel(resp2);
                aluno3.adicionarResponsavel(resp3);

                alunoDAO.cadastrar(aluno1);
                alunoDAO.cadastrar(aluno2);
                alunoDAO.cadastrar(aluno3);

                alunos = alunoDAO.listarTodos();

                // Funcionários
                Professor prof1 = new Professor(nextFuncionarioId++, "Amanda Rocha", 28, "777.888.999-00", "CLT", "Manhã", false);
                Professor prof2 = new Professor(nextFuncionarioId++, "Bruno Almeida", 32, "888.999.000-11", "CLT", "Tarde", false);
                Assistente assistente1 = new Assistente(nextFuncionarioId++, "Carlos Souza", 25, "999.000.111-22", "CLT", "2-3 anos", false);

                funcionarioDAO.cadastrar(prof1);
                funcionarioDAO.cadastrar(prof2);
                funcionarioDAO.cadastrar(assistente1);

                funcionarios = funcionarioDAO.listarTodos();

                JOptionPane.showMessageDialog(frame,
                        "Dados iniciais cadastrados no banco!\n\n" +
                                "Cadastrados:\n" +
                                "• " + alunos.size() + " alunos\n" +
                                "• " + funcionarios.size() + " funcionários\n" +
                                "• " + responsaveis.size() + " responsáveis");

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame,
                        "Erro ao cadastrar dados iniciais: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}