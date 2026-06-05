import { createTheme } from '@mui/material/styles'

export const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#19d3a2',
    },
    secondary: {
      main: '#4f9dff',
    },
    background: {
      default: '#07111f',
      paper: '#0d1829',
    },
    success: {
      main: '#2dd4bf',
    },
    warning: {
      main: '#f59e0b',
    },
    error: {
      main: '#ef4444',
    },
  },
  shape: {
    borderRadius: 20,
  },
  typography: {
    fontFamily: 'Manrope, system-ui, sans-serif',
    h1: {
      fontFamily: 'Space Grotesk, Manrope, sans-serif',
      fontWeight: 700,
    },
    h2: {
      fontFamily: 'Space Grotesk, Manrope, sans-serif',
      fontWeight: 700,
    },
    h3: {
      fontFamily: 'Space Grotesk, Manrope, sans-serif',
      fontWeight: 700,
    },
    h4: {
      fontFamily: 'Space Grotesk, Manrope, sans-serif',
      fontWeight: 700,
    },
    button: {
      textTransform: 'none',
      fontWeight: 700,
    },
  },
  components: {
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'linear-gradient(180deg, rgba(255,255,255,0.04), rgba(255,255,255,0.01))',
          border: '1px solid rgba(255,255,255,0.08)',
          backdropFilter: 'blur(16px)',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          paddingTop: 14,
          paddingBottom: 14,
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiInputBase-root': {
            borderRadius: 16,
          },
        },
      },
    },
  },
})
