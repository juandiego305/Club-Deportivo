package com.juandiego.tactica.data

/** Pares (mes, fecha de pago). Fecha null = mensualidad registrada pero sin pagar. */
private fun fees(vararg items: Pair<String, String?>) =
    items.map { (month, date) -> PaymentRecord(month, date != null, date) }

private fun payments(enrollmentDate: String?, fees: List<PaymentRecord>) = PlayerPayments(
    enrollmentPaid = enrollmentDate != null,
    enrollmentDate = enrollmentDate,
    enrollmentAmount = DEFAULT_ENROLLMENT,
    monthlyAmount = DEFAULT_MONTHLY,
    monthlyFees = fees,
)

fun starterTeams(): List<Team> = listOf(
    Team(
        id = 1, sport = Sport.FUTBOL, name = "Atlético Norte", category = "Primera masculina",
        players = listOf(
            Player(1, "Carlos Mendoza", 1, "Portero", true, payments("2026-01-15", fees(
                "2026-06" to "2026-06-05", "2026-07" to "2026-07-04", "2026-08" to "2026-08-06", "2026-09" to "2026-09-04",
            ))),
            Player(2, "Mateo Ríos", 4, "Defensa", true, payments("2026-01-20", fees(
                "2026-06" to "2026-06-10", "2026-07" to "2026-07-08", "2026-08" to "2026-08-12", "2026-09" to null,
            ))),
            Player(3, "Diego López", 8, "Mediocentro", false, payments("2026-02-01", fees(
                "2026-06" to "2026-06-15", "2026-07" to null, "2026-08" to null, "2026-09" to null,
            ))),
            Player(4, "Leo Torres", 10, "Delantero", true, payments(null, emptyList())),
        ),
        events = listOf(
            TeamEvent(1, EventType.ENTRENAMIENTO, "Entrenamiento táctico", "2026-05-16", "19:00", "Campo 2"),
            TeamEvent(2, EventType.PARTIDO, "vs. Deportivo Sur", "2026-05-18", "17:30", "Estadio Municipal"),
        ),
        matches = listOf(
            Match(1, "Unión Central", "2026-05-10", 3, 1),
            Match(2, "Real Oeste", "2026-05-03", 2, 2),
            Match(3, "Sporting Azul", "2026-04-26", 0, 1),
        ),
    ),
    Team(
        id = 2, sport = Sport.VOLEIBOL, name = "Atenea Vóley", category = "Senior femenina",
        players = listOf(
            Player(5, "Laura Vidal", 2, "Colocadora", true, payments("2026-02-10", fees(
                "2026-06" to "2026-06-08", "2026-07" to "2026-07-07", "2026-08" to "2026-08-05", "2026-09" to "2026-09-02",
            ))),
            Player(6, "Marta Gil", 7, "Central", true, payments("2026-03-01", fees(
                "2026-06" to "2026-06-12", "2026-07" to "2026-07-14", "2026-08" to "2026-08-10", "2026-09" to null,
            ))),
            Player(7, "Sofía Luna", 10, "Opuesta", true, payments("2026-02-25", fees(
                "2026-06" to "2026-06-06", "2026-07" to "2026-07-05", "2026-08" to "2026-08-04", "2026-09" to "2026-09-03",
            ))),
            Player(8, "Alba Martín", 12, "Líbero", false, payments("2026-03-15", fees(
                "2026-06" to "2026-06-20", "2026-07" to null, "2026-08" to null, "2026-09" to null,
            ))),
        ),
        events = listOf(
            TeamEvent(3, EventType.ENTRENAMIENTO, "Entrenamiento técnico", "2026-05-17", "20:00", "Pista A"),
            TeamEvent(4, EventType.PARTIDO, "vs. Club Costa", "2026-05-19", "11:00", "Pabellón Central"),
        ),
        matches = listOf(
            Match(4, "Vóley Centro", "2026-05-11", 3, 0),
            Match(5, "Club Marina", "2026-05-04", 3, 2),
            Match(6, "Estrella Sur", "2026-04-27", 1, 3),
        ),
    ),
)
