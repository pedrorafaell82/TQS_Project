/**
 * Gestão de Instrumentos do Owner
 * User Story: SOUN-24 – Instrument Listings Management
 */

import React, { useEffect, useState } from "react";
import { getInstruments, updateInstrumentStatus } from "../../services/api";

export default function InstrumentManagement() {
    const [instruments, setInstruments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Carrega a lista de instrumentos
    useEffect(() => {
        carregarInstrumentos();
    }, []);

    const carregarInstrumentos = async () => {
        try {
            setLoading(true);
            const response = await getInstruments();
            setInstruments(response.data);
            setError(null);
        } catch (err) {
            console.error(err);
            setError("Não foi possível carregar os instrumentos.");
        } finally {
            setLoading(false);
        }
    };

    // Ativa ou desativa um instrumento
    const alterarEstado = async (instrumento) => {
        try {
            await updateInstrumentStatus(instrumento.id, !instrumento.active);
            carregarInstrumentos(); 
        } catch (err) {
            console.error(err);
            alert("Erro ao atualizar o estado do instrumento.");
        }
    };

    if (loading) {
        return <p style={{ padding: "2rem" }}>A carregar instrumentos...</p>;
    }

    if (error) {
        return <p style={{ padding: "2rem", color: "red" }}>{error}</p>;
    }

    return (
        <div style={{ padding: "2rem" }}>
            <h2>Owner – My Instruments</h2>

            {instruments.length === 0 ? (
                <p>Não existem instrumentos registados.</p>
            ) : (
                <table border="1" cellPadding="8" style={{ marginTop: "1rem", width: "100%" }}>
                    <thead>
                        <tr>
                            <th>Nome</th>
                            <th>Preço Diário (€)</th>
                            <th>Categoria</th>
                            <th>Estado</th>
                            <th>Ação</th>
                        </tr>
                    </thead>
                    <tbody>
                        {instruments.map(inst => (
                            <tr key={inst.id}>
                                <td>{inst.name}</td>
                                <td>{inst.dailyPrice}</td>
                                <td>{inst.category}</td>
                                <td>{inst.active ? "Ativo" : "Inativo"}</td>
                                <td>
                                    <button onClick={() => alterarEstado(inst)}>
                                        {inst.active ? "Desativar" : "Ativar"}
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}
