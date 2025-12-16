/**
 * User Story: SOUN-22 - Platform Overview Dashboard
 * As an admin I want to see main platform KPIs so that I can monitor business health.
 */

import React, { useEffect, useState } from "react";
import { getPlatformOverview } from "../../services/adminService";
import "./PlatformOverviewDashboard.css";

export default function PlatformOverviewDashboard() {
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadMetrics = async () => {
      try {
        setLoading(true);
        const data = await getPlatformOverview();
        setMetrics(data);
        setError(null);
      } catch (err) {
        setError("Unable to load platform KPIs");
      } finally {
        setLoading(false);
      }
    };

    loadMetrics();
  }, []);

  if (loading) {
    return <div className="kpi-loading">Loading KPIs...</div>;
  }

  if (error) {
    return <div className="kpi-error">{error}</div>;
  }

  if (!metrics) {
    return <div className="kpi-error">No data available.</div>;
  }

  return (
    <div className="kpi-dashboard">
      <h2>Platform Overview</h2>
      <p className="kpi-subtitle">
        Key performance indicators to monitor SoundShop business health.
      </p>

      <div className="kpi-cards-grid">
        <div className="kpi-card">
          <h3>Total Users</h3>
          <p className="kpi-value">{metrics.totalUsers}</p>
          <span className="kpi-label">Owners and renters</span>
        </div>

        <div className="kpi-card">
          <h3>Total Instruments</h3>
          <p className="kpi-value">{metrics.totalInstruments}</p>
          <span className="kpi-label">Registered items</span>
        </div>

        <div className="kpi-card">
          <h3>Active Rentals</h3>
          <p className="kpi-value">{metrics.activeRentals}</p>
          <span className="kpi-label">Currently ongoing</span>
        </div>

        <div className="kpi-card">
          <h3>Completed Rentals</h3>
          <p className="kpi-value">{metrics.completedRentals}</p>
          <span className="kpi-label">Finished rentals</span>
        </div>

        <div className="kpi-card kpi-revenue">
          <h3>Total Revenue</h3>
          <p className="kpi-value">
            €{Number(metrics.totalRevenue || 0).toFixed(2)}
          </p>
          <span className="kpi-label">Platform revenue</span>
        </div>
      </div>
    </div>
  );
}
